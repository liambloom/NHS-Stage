<#
.SYNOPSIS
Used to package the program into an installer using the jpacakge utility

.PARAMETER Types
One or more pacakge types. Each is run as a separate instance of jpackage, and 
is passed in a --type. The "app-image" type is pacakged regardless of this parameter's
value. Valid values are {"exe", "msi", "rpm", "deb", "pkg", "dmg", "all"}

.EXAMPLE
package -Types exe, msi
Pacakges the application, producing an app-image, exe installer, and msi installer
#>




param(
    [Parameter(Position = 0)]
    [ValidateSet('exe', 'msi', 'rpm', 'deb', 'pkg', 'dmg', 'all')]
    [string[]]$Types,

    [ValidateSet('Compile', 'Dependencies', 'Image', 'Installer')]
    [string]$JumpTo="Compile",

    [switch]$KeepDependencies,

    [ValidateSet('Debian', 'Other')]
    [string]$Distro
)

enum Stage {
    Compile
    Dependencies
    Image
    Installer
}

$StartStage = [Stage]$JumpTo
$Verbose = $PSCmdlet.MyInvocation.BoundParameters["Verbose"].IsPresent
$MvnVerbose = if ($Verbose) { $null } else {'-q'}
$Deps="target/temp-dependencies"

if (($IsLinux -ne $true) -and $PSBoundParameters.ContainsKey('Distro')) {
    throw "Distro should only be used on linux"
}
elseif ((($IsLinux -eq $true) -and (-not $PSBoundParameters.ContainsKey('Distro')))) {
    throw "Distro must be defined on linux as either 'debian' or 'other'"
}

if ($StartStage -le [Stage]::Compile) {
    Write-Verbose "Beginning compile stage"
    Write-Verbose "packaging to jar..."
    mvn clean compile package $MvnVerbose
    Write-Verbose "done packaging to jar"
}
else {
    Write-Verbose "Skipping compile stage"
}

if ($StartStage -le [Stage]::Dependencies) {
    Write-Verbose "Beginning dependencies stage"
    Write-Verbose "copying dependencies..."
    mvn dependency:copy-dependencies -DoutputDirectory="$Deps" $MvnVerbose
    Write-Verbose "done copying dependencies..."

    Write-Verbose "making all depenencies modular..."
    Set-Location $Deps
    Get-ChildItem . | ForEach-Object {
        Write-Verbose "checking if $($_.Name) is modular"
        if (@(jar -d -f "$($_.FullName)")[0] | Select-String -Pattern 'automatic' -SimpleMatch -Quiet) {
            Write-Verbose "$($_.Name) is not modular"
            mkdir "staging" | Out-Null
            Set-Location "staging"
            jar xf "$($_.FullName)"
            $ContainsClasses = @(Get-ChildItem -Path . -Filter *.class -Recurse).Length -ne 0
            Set-Location ..

            if ($ContainsClasses) {
                Write-Verbose "making $($_.Name) modular"

                jdeps $MvnVerbose --generate-module-info module-info "$($_.FullName)"

                Move-Item module-info/*/module-info.java module-info/module-info.java
                Remove-Item module-info/* -Force -Recurse -Exclude module-info.java
                Get-ChildItem staging/* -Directory | Copy-Item -Destination module-info -Filter {PSIsContainer} -Recurse

                $Packages = Get-ChildItem module-info -Directory -Recurse
                :packageLoop foreach ($Package in $Packages) {
                    $PackageName = ''
                    $PackageDir = $Package
                    while ($PackageDir.FullName -ne (Get-Item module-info).FullName) {
                        if (-Not ($PackageDir.Name | Select-String -Pattern '^[a-z$_][a-z$_0-9]*$' -Quiet)) {
                            continue packageLoop
                        }

                        $PackageName = "$($PackageDir.Name).$PackageName"
                        $PackageDir = $PackageDir.Parent
                    }

                    $PackageName = $PackageName.Substring(0, $PackageName.Length - 1)
                    New-Item "$($Package.FullName)/Foo.java" -ItemType File -Value "package $PackageName ; public class Foo { }" | Out-Null
                }

                javac $(if ($Verbose) { '-verbose' } else { $null }) -d module-info (Get-ChildItem module-info/* -Recurse -File | Select-Object -ExpandProperty FullName)
                if (Test-Path module-info/module-info.class) {
                    Write-Verbose "Compiled module-info.java successfully"
                }
                else {
                    Write-Verbose "Failed to compile module-info.java"
                }

                Move-Item module-info/module-info.class staging/module-info.class
                Remove-Item -force -r module-info
            
                Remove-Item "$($_.FullName)"

                jar -cf "$($_.FullName)" -C staging .

                Write-Verbose "$($_.Name) is now modular"
            }
            else {
                Write-Verbose "$($_.Name) is empty, moving on"
            }

            Remove-Item -force -r staging


        }
        else {
            Write-Verbose "$($_.Name) is modular"
        }
    }

    Set-Location ../..
}
else {
    Write-Verbose "Skipping dependencies stage"
}

# $IsWindows was added in Powershell 6.0, but I want this to run on powershell 5.1, which 
#  is what is shipped with windows by default. Hoewever, I can safely use $IsMacOS and $IsLinux
#  because, as far as I can tell, 6.0 is the minimum version of powershell that supports
#  any OS other than windows.

# Icon types: win: ico; mac: icns; linux: png
if ([System.Environment]::OSVersion.Platform -eq "Win32NT") {
    $OS = "windows"
    $IconType = "ico"
    $OSArgs = "--win-menu", "--win-dir-chooser"
    if ($Types -eq "all") {
        $Types = "msi", "exe"
    }
}
elseif ($IsMacOS) {
    $OS = "macos"
    $IconType = "icns"
    if ($Types -eq "all") {
        $Types = "deb", "rpm"
    }
}
elseif ($IsLinux) {
    if ($Distro -eq 'Debian') {
        $OS = "linux/debian"
    }
    else {
        $OS = "linux/rpm"
    }
    $IconType = "png"
    $OSArgs = '--linux-menu-group', 'Utility;Java;', "--linux-shortcut", '--linux-app-category', 'misc'
    if ($Types -eq "all") {
        if ($Distro -eq 'Debian') {
            $Types = "deb"
        }
        else {
            $Types = "rpm"
        }
    }
}
else {
    throw "Unrecognized OS"
}

$name='Stage Builder For NHS'
$Version="$(mvn help:evaluate '-Dexpression=project.version' -q -DforceStdout)"
$Dest="packaged/$Version/$OS"
$ImageLoc="$Dest/$name"

if ($StartStage -le [Stage]::Image) {
    Write-Verbose "Beginning image stage"
    Write-Verbose "generating application image..."
    Remove-Item -Force -Recurse -Path "$Dest" -ErrorAction SilentlyContinue

    Write-Verbose "Packaging app-image"
    jpackage --name "$name" --app-version $Version --icon "src/icon/icon.$IconType" `
        --dest "$Dest" --type app-image `
        --module-path target/temp-dependencies --module-path target/package.jar `
        --module dev.liambloom.nhs.inductionStage/dev.liambloom.nhs.inductionStage.gui.StageManager

    mkdir "$ImageLoc/legal" | Out-Null
    Copy-Item -Path ./DEPENDENCY-LICENSES/* , ./LICENSE , ./NOTICE -Destination "$ImageLoc/legal"

    Write-Verbose "done generating application image"

    Write-Verbose "create image archive..."

    if ([System.Environment]::OSVersion.Platform -eq "Win32NT") {
        Compress-Archive -Path "$ImageLoc" -DestinationPath "$Dest/$name.zip"
    }
    elseif ($IsLinux) {
        tar -czf "$Dest/$($name.replace(' ', '-')).tar.gz" "$ImageLoc"
    }  

    Write-Verbose "done creating image archive"
}

if ($StartStage -le [Stage]::Installer) {
    Write-Verbose "Beginning installer stage"
    foreach ($Type in $Types) {
        Write-Verbose "Packaging $Type"
        jpackage --app-version $Version  --vendor 'Liam Bloom' --copyright '(C) Liam Bloom 2023' `
                --dest "$Dest" --app-image "$ImageLoc" --type $Type `
                --license-file ./LICENSE --about-url 'https://github.com/liambloom/NHS-Stage' `
                $OSArgs
    }
}

if ($KeepDependencies -eq $false) {
    Write-Verbose "cleanup..."
    
    Remove-Item -Force -Recurse "$Deps"
    
    Write-Verbose "done!"
}
else {
    Write-Verbose "Skipping cleanup"
}
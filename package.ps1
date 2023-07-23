<#
.SYNOPSIS
Used to package the program into an installer using the jpacakge utility

.PARAMETER Types
One or more pacakge types. Each is run as a separate instance of jpackage, and 
is passed in a --type. The "app-image" type is pacakged regardless of this parameter's
value. Valid values (as of JDK 17) are {"app-image", "exe", "msi", "rpm", "deb", "pkg", "dmg"}

.EXAMPLE
package -Types exe, msi
Pacakges the application at version 1.2, producing an app-image, exe installer, and msi installer
#>

[CmdletBinding(PositionalBinding=$false)]
param([switch]$DebugMode, [string[]]$Types="app-image")

function Write-Debug {
    param($content)
    if ($DebugMode) {
        Write-Output $content
    }
}
Set-Alias debug Write-Debug

$deps="target/temp-dependencies"

debug "packaging..."
mvn clean compile package -q
debug "done packaging"

debug "copying dependencies..."
mvn dependency:copy-dependencies -DoutputDirectory="$deps" -q
debug "done copying dependencies..."

debug "making all depenencies modular..."
Set-Location $deps
Get-ChildItem . | ForEach-Object {
    debug "checking if $($_.Name) is modular"
    if (@(jar -d -f "$($_.FullName)")[0] | Select-String -Pattern 'automatic' -SimpleMatch -Quiet) {
        debug "$($_.Name) is not modular"
        mkdir "staging" | Out-Null
        Set-Location "staging"
        jar xf "$($_.FullName)"
        $ContainsClasses = @(Get-ChildItem -Path . -Filter *.class -Recurse).Length -ne 0
        Set-Location ..

        if ($ContainsClasses) {
            debug "making $($_.Name) modular"

            jdeps -q --generate-module-info module-info "$($_.FullName)"

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

            javac -d module-info (Get-ChildItem module-info\* -Recurse -File | Select-Object -ExpandProperty FullName)
            if (Test-Path module-info/module-info.class) {
                debug "Compiled module-info.java successfully"
            }
            else {
                debug "Failed to compile module-info.java"
            }

            Move-Item module-info/module-info.class staging/module-info.class
            Remove-Item -force -r module-info
        
            Remove-Item "$($_.FullName)"

            jar -cf "$($_.FullName)" -C staging .

            debug "$($_.Name) is now modular"
        }
        else {
            debug "$($_.Name) is empty, moving on"
        }

        Remove-Item -force -r staging


    }
    else {
        debug "$($_.Name) is modular"
    }
}

Set-Location ../..

debug "packaging..."


# $IsWindows was added in Powershell 6.0, but I want this to run on powershell 5.1, which 
#  is what is shipped with windows by default. Hoewever, I can safely use $IsMacOS and $IsLinux
#  because, as far as I can tell, 6.0 is the minimum version of powershell that supports
#  any OS other than windows.

# Icon types: win: ico; mac: icns; linux: png
if ([System.Environment]::OSVersion.Platform -eq "Win32NT") {
    $OS = "windows"
    $icon = "src/icon/icon.ico"
}
elseif ($IsMacOS) {
    $OS = "macos"
}
elseif ($IsLinux) {
    $OS = "linux"
}
else {
    $OS = "other"
}

$name='Stage Builder For NHS'
$imageLoc="packaged/$OS/$name"
$Version="$(mvn help:evaluate '-Dexpression=project.version' -q -DforceStdout)"

Remove-Item -Force -Recurse -Path "packaged/$OS" -ErrorAction SilentlyContinue

# TODO: --icon
jpackage --name "$name" --app-version $Version --icon "$icon" `
    --dest "packaged/$OS" --type app-image `
    --module-path target/temp-dependencies --module-path target/package.jar `
    --module dev.liambloom.nhs.inductionStage/dev.liambloom.nhs.inductionStage.gui.StageManager

mkdir "$imageLoc/legal" | Out-Null
Copy-Item -Path ./DEPENDENCY-LICENSES/* , ./LICENSE , ./NOTICE -Destination "$imageLoc/legal"

foreach ($Type in $Types) {
    if ($Type -ne "app-image") {
        Remove-Item -Force -Path "packaged/$OS/$name*.$Type" -ErrorAction SilentlyContinue

        jpackage --app-version $Version  --vendor "Liam Bloom" --copyright "(C) Liam Bloom 2023" `
            --dest "packaged/$OS" --app-image "$imageLoc" --type $Type `
            --license-file .\LICENSE --about-url "https://github.com/liambloom/NHS-Stage" `
            --win-menu --win-dir-chooser
    }
}


debug "done packaging"

debug "cleanup"

Remove-Item -Force -Recurse "$deps"
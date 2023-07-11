[CmdletBinding(PositionalBinding=$false)]
param([string]$Version='1.0', [switch]$DebugMode)

function Write-Debug {
    param($content)
    if ($DebugMode) {
        echo $content
    }
}
Set-Alias debug Write-Debug

$deps="target/temp-dependencies"

debug "packaging..."
mvn clean package -q
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
            xcopy staging\* module-info /t /e

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

jpackage --name 'Stage Builder For NHS' --app-version $Version `
    --module-path target/temp-dependencies --module-path target/package.jar `
    --module dev.liambloom.nhs.inductionStage/dev.liambloom.nhs.inductionStage.gui.StageManager 

debug "done packaging"

debug "cleanup"

Remove-Item -Force -Recurse "$deps"
param(
    [Parameter(Mandatory=$true)]
    [string]$InputFile,

    [Parameter(Mandatory=$true)]
    [string]$OutputDir
)

$ErrorActionPreference = "Stop"

Write-Host "PowerShell script started..."
Write-Host "Input file: $InputFile"
Write-Host "Output folder: $OutputDir`n"

try {
    $Colors = 'Black','Blue','BlueDark','Brown','Orange','Pink','Purple','Red','White','Yellow'

    if (-not (Test-Path $InputFile)) { throw "Input file '$InputFile' does not exist!" }

    # Load Source
    $Source = Get-Content $InputFile -Raw | ConvertFrom-Json

    if (-not (Test-Path $OutputDir)) {
        Write-Host "Creating output directory..."
        New-Item -ItemType Directory -Path $OutputDir | Out-Null
    }

    # DeepCopy function - we use -AsHashtable to ensure we don't lose object structure
    function DeepCopy($obj) {
        return ($obj | ConvertTo-Json -Depth 100 | ConvertFrom-Json)
    }

    # Recursive Function to find and replace GradientId
    function Replace-GradientId {
        param(
            [Parameter(Mandatory=$true)]$Target,
            [Parameter(Mandatory=$true)][string]$Color
        )

        if ($null -eq $Target) { return }

        # If it's an array/list, iterate through it
        if ($Target -is [System.Collections.IEnumerable] -and $Target -isnot [string]) {
            foreach ($item in $Target) {
                Replace-GradientId -Target $item -Color $Color
            }
        }
        # If it's an object, check properties
        else {
            $props = $Target.psobject.Properties
            foreach ($prop in $props) {
                if ($prop.Name -eq "GradientId") {
                    $Target.GradientId = $Color
                }
                # Recurse if the property itself is an object or collection
                elseif ($prop.Value -is [System.Collections.IEnumerable] -or ($null -ne $prop.Value.psobject)) {
                    if ($prop.Value -isnot [string]) {
                        Replace-GradientId -Target $prop.Value -Color $Color
                    }
                }
            }
        }
    }

    Write-Host "Starting to generate JSON files..."
    $total = $Colors.Count
    $count = 0

    foreach ($Color in $Colors) {
        $count++
        Write-Host "[$count/$total] Processing color: $Color"

        # 1. Create a fresh copy of the whole RandomAttachmentSets object
        $AttachmentSetsCopy = DeepCopy $Source.RandomAttachmentSets
        
        # 2. Update all GradientId values within that object
        Replace-GradientId -Target $AttachmentSetsCopy -Color $Color

        # 3. Construct the final JSON structure
        $OutputObject = [ordered]@{
            Parent               = "Chocobo"
            GradientId           = $Color
            RandomAttachmentSets = $AttachmentSetsCopy
        }

        $BaseName = [System.IO.Path]::GetFileNameWithoutExtension($InputFile)
        $OutFile = Join-Path $OutputDir ("$BaseName" + "_" + "$Color.json")

        # 4. Save the file
        $Utf8NoBom = New-Object System.Text.UTF8Encoding($false)
        $JsonContent = $OutputObject | ConvertTo-Json -Depth 100
        [System.IO.File]::WriteAllText($OutFile, $JsonContent, $Utf8NoBom)
    }

    Write-Host "`nAll $total files generated successfully in: $OutputDir"
}
catch {
    Write-Host "`nERROR: $($_.Exception.Message)"
    Write-Host $_.ScriptStackTrace
}
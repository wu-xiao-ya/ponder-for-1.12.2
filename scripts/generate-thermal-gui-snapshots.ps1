param(
    [string]$SourceDir = (Join-Path $PSScriptRoot '..\..\temp_gui_inspect'),
    [string]$OutputDir = (Join-Path $PSScriptRoot '..\src\main\resources\assets\ponder\textures\gui\thermalexpansion')
)

Set-StrictMode -Version Latest
$ErrorActionPreference = 'Stop'

Add-Type -AssemblyName System.Drawing

function New-ArgbBitmap([int]$Width, [int]$Height) {
    return New-Object System.Drawing.Bitmap($Width, $Height, [System.Drawing.Imaging.PixelFormat]::Format32bppArgb)
}

function New-Graphics([System.Drawing.Bitmap]$Bitmap) {
    $graphics = [System.Drawing.Graphics]::FromImage($Bitmap)
    $graphics.InterpolationMode = [System.Drawing.Drawing2D.InterpolationMode]::NearestNeighbor
    $graphics.PixelOffsetMode = [System.Drawing.Drawing2D.PixelOffsetMode]::Half
    $graphics.SmoothingMode = [System.Drawing.Drawing2D.SmoothingMode]::None
    $graphics.CompositingMode = [System.Drawing.Drawing2D.CompositingMode]::SourceOver
    $graphics.Clear([System.Drawing.Color]::Transparent)
    return $graphics
}

function Draw-Crop(
    [System.Drawing.Graphics]$Graphics,
    [System.Drawing.Image]$Image,
    [int]$SourceX,
    [int]$SourceY,
    [int]$SourceWidth,
    [int]$SourceHeight,
    [int]$DestX,
    [int]$DestY,
    [int]$DestWidth = $SourceWidth,
    [int]$DestHeight = $SourceHeight
) {
    $destRect = New-Object System.Drawing.Rectangle($DestX, $DestY, $DestWidth, $DestHeight)
    $srcRect = New-Object System.Drawing.Rectangle($SourceX, $SourceY, $SourceWidth, $SourceHeight)
    $Graphics.DrawImage($Image, $destRect, $srcRect, [System.Drawing.GraphicsUnit]::Pixel)
}

function Draw-TabBackground(
    [System.Drawing.Graphics]$Graphics,
    [System.Drawing.Image]$Texture,
    [int]$DestX,
    [int]$DestY,
    [int]$Width,
    [int]$Height
) {
    $border = 4
    $rightSourceX = 256 - $Width + $border
    $bottomSourceY = 256 - $Height + $border
    Draw-Crop $Graphics $Texture 0 0 $border $border $DestX $DestY
    Draw-Crop $Graphics $Texture $rightSourceX 0 ($Width - $border) $border ($DestX + $border) $DestY
    Draw-Crop $Graphics $Texture 0 $bottomSourceY $border ($Height - $border) $DestX ($DestY + $border)
    Draw-Crop $Graphics $Texture $rightSourceX $bottomSourceY ($Width - $border) ($Height - $border) ($DestX + $border) ($DestY + $border)
}

function Draw-EnergyBar(
    [System.Drawing.Graphics]$Graphics,
    [System.Drawing.Image]$Texture,
    [int]$DestX,
    [int]$DestY,
    [int]$Scaled
) {
    Draw-Crop $Graphics $Texture 0 0 16 42 $DestX $DestY
    if ($Scaled -gt 0) {
        $clamped = [Math]::Max(0, [Math]::Min(42, $Scaled))
        $srcY = 42 - $clamped
        $dstY = $DestY + 42 - $clamped
        Draw-Crop $Graphics $Texture 16 $srcY 16 $clamped $DestX $dstY
    }
}

function Draw-DualScaledHorizontal(
    [System.Drawing.Graphics]$Graphics,
    [System.Drawing.Image]$Texture,
    [int]$DestX,
    [int]$DestY,
    [int]$Width,
    [int]$Height,
    [int]$Quantity
) {
    Draw-Crop $Graphics $Texture 0 0 $Width $Height $DestX $DestY
    $clamped = [Math]::Max(0, [Math]::Min($Width, $Quantity))
    if ($clamped -gt 0) {
        Draw-Crop $Graphics $Texture $Width 0 $clamped $Height $DestX $DestY
    }
}

function Draw-DualScaledVertical(
    [System.Drawing.Graphics]$Graphics,
    [System.Drawing.Image]$Texture,
    [int]$DestX,
    [int]$DestY,
    [int]$Width,
    [int]$Height,
    [int]$Quantity
) {
    Draw-Crop $Graphics $Texture 0 0 $Width $Height $DestX $DestY
    $clamped = [Math]::Max(0, [Math]::Min($Height, $Quantity))
    if ($clamped -gt 0) {
        $srcY = $Height - $clamped
        $dstY = $DestY + $Height - $clamped
        Draw-Crop $Graphics $Texture $Width $srcY $Width $clamped $DestX $dstY
    }
}

function Draw-SlotOverlay(
    [System.Drawing.Graphics]$Graphics,
    [System.Drawing.Image]$Texture,
    [string]$Color,
    [string]$Type,
    [int]$PosX,
    [int]$PosY
) {
    $colorOrdinals = @{
        BLUE   = 0
        RED    = 1
        YELLOW = 2
        ORANGE = 3
        GREEN  = 4
        PURPLE = 5
    }
    $typeOrdinals = @{
        STANDARD      = 0
        OUTPUT        = 1
        OUTPUT_DOUBLE = 2
        TANK          = 3
        TANK_SHORT    = 4
    }

    $colorOrdinal = $colorOrdinals[$Color]
    $typeOrdinal = $typeOrdinals[$Type]
    $sourceX = [Math]::Floor($colorOrdinal / 3) * 128 + $typeOrdinal * 32
    $sourceY = ($colorOrdinal % 3) * 32
    $width = 32
    $height = 32
    $destX = $PosX
    $destY = $PosY

    switch ($Type) {
        'STANDARD' {
            $destX -= 8
            $destY -= 8
        }
        'OUTPUT' {
            $destX -= 4
            $destY -= 4
        }
        'OUTPUT_DOUBLE' {
            $width = 64
            $destX -= 11
            $destY -= 4
        }
        'TANK' {
            $height = 64
            $sourceX = $colorOrdinal * 32
            $sourceY = 96
            $destX -= 8
            $destY -= 2
        }
        'TANK_SHORT' {
            $height = 34
            $sourceX = $colorOrdinal * 32
            $sourceY = 160
            $destX -= 8
            $destY -= 2
        }
    }

    Draw-Crop $Graphics $Texture $sourceX $sourceY $width $height $destX $destY
}

function Fill-TabPanelInterior(
    [System.Drawing.Graphics]$Graphics,
    [System.Drawing.Image]$Texture,
    [int]$PanelX,
    [int]$PanelY,
    [int]$DestX,
    [int]$DestY,
    [int]$Width,
    [int]$Height
) {
    Draw-Crop $Graphics $Texture 16 20 $Width $Height ($PanelX + $DestX) ($PanelY + $DestY)
}

function Draw-HeaderIcon(
    [System.Drawing.Graphics]$Graphics,
    [System.Drawing.Image]$Texture,
    [int]$DestX,
    [int]$DestY,
    [int]$Width,
    [int]$Height,
    [int]$SourceX = 0,
    [int]$SourceY = 0,
    [int]$SourceWidth = 0,
    [int]$SourceHeight = 0
) {
    if ($SourceWidth -le 0) {
        $SourceWidth = $Texture.Width
    }
    if ($SourceHeight -le 0) {
        $SourceHeight = $Texture.Height
    }
    Draw-Crop $Graphics $Texture $SourceX $SourceY $SourceWidth $SourceHeight $DestX $DestY $Width $Height
}

function Save-Bitmap([System.Drawing.Bitmap]$Bitmap, [string]$Path) {
    $directory = Split-Path -Parent $Path
    if (!(Test-Path $directory)) {
        New-Item -ItemType Directory -Path $directory | Out-Null
    }
    $Bitmap.Save($Path, [System.Drawing.Imaging.ImageFormat]::Png)
}

function Save-CroppedBitmap(
    [System.Drawing.Image]$Source,
    [int]$SourceX,
    [int]$SourceY,
    [int]$Width,
    [int]$Height,
    [string]$Path
) {
    $bitmap = New-ArgbBitmap $Width $Height
    $graphics = New-Graphics $bitmap
    try {
        Draw-Crop $graphics $Source $SourceX $SourceY $Width $Height 0 0
        Save-Bitmap $bitmap $Path
    } finally {
        $graphics.Dispose()
        $bitmap.Dispose()
    }
}

$furnaceBase = [System.Drawing.Bitmap]::FromFile((Join-Path $SourceDir 'furnace.png'))
$energy = [System.Drawing.Bitmap]::FromFile((Join-Path $SourceDir 'energy.png'))
$slots = [System.Drawing.Bitmap]::FromFile((Join-Path $SourceDir 'slots.png'))
$arrow = [System.Drawing.Bitmap]::FromFile((Join-Path $SourceDir 'progress_arrow_right.png'))
$flame = [System.Drawing.Bitmap]::FromFile((Join-Path $SourceDir 'scale_flame.png'))
$tabRight = [System.Drawing.Bitmap]::FromFile((Join-Path $SourceDir 'tab_right.png'))
$buttonEnabled = [System.Drawing.Bitmap]::FromFile((Join-Path $SourceDir 'button_enabled.png'))
$buttonDisabled = [System.Drawing.Bitmap]::FromFile((Join-Path $SourceDir 'button_disabled.png'))
$buttons = [System.Drawing.Bitmap]::FromFile((Join-Path $SourceDir 'buttons.png'))
$infoSignal = [System.Drawing.Bitmap]::FromFile((Join-Path $SourceDir 'info_signal.png'))
$infoOutput = [System.Drawing.Bitmap]::FromFile((Join-Path $SourceDir 'info_output.png'))
$slotGridAugment = [System.Drawing.Bitmap]::FromFile((Join-Path $SourceDir 'slot_grid_augment.png'))
$machineFace = [System.Drawing.Bitmap]::FromFile((Join-Path $SourceDir 'machine\machine_face_furnace.png'))
$machineSide = [System.Drawing.Bitmap]::FromFile((Join-Path $SourceDir 'machine\machine_side.png'))
$machineTop = [System.Drawing.Bitmap]::FromFile((Join-Path $SourceDir 'machine\machine_top.png'))
$machineBottom = [System.Drawing.Bitmap]::FromFile((Join-Path $SourceDir 'machine\machine_bottom.png'))

try {
    $baseWidth = 198
    $baseHeight = 166

    $baseBitmap = New-ArgbBitmap $baseWidth $baseHeight
    $baseGraphics = New-Graphics $baseBitmap
    try {
        Draw-Crop $baseGraphics $furnaceBase 0 0 176 166 0 0
        Draw-EnergyBar $baseGraphics $energy 8 8 30
        Draw-SlotOverlay $baseGraphics $slots 'BLUE' 'STANDARD' 53 26
        Draw-SlotOverlay $baseGraphics $slots 'ORANGE' 'OUTPUT' 112 31
        Draw-DualScaledHorizontal $baseGraphics $arrow 79 34 24 16 18
        Draw-DualScaledVertical $baseGraphics $flame 53 44 16 16 10

        Draw-TabBackground $baseGraphics $tabRight 176 6 22 22
        Draw-TabBackground $baseGraphics $tabRight 176 30 22 22
        Draw-TabBackground $baseGraphics $tabRight 176 54 22 22

        Draw-HeaderIcon $baseGraphics $infoSignal 182 11 10 10
        Draw-HeaderIcon $baseGraphics $machineSide 181 35 10 10
        Draw-HeaderIcon $baseGraphics $slotGridAugment 181 59 10 10 0 0 18 18
    } finally {
        $baseGraphics.Dispose()
    }
    Save-Bitmap $baseBitmap (Join-Path $OutputDir 'furnace_snapshot_base.png')

    $redstoneWidth = 288
    $redstoneBitmap = New-ArgbBitmap $redstoneWidth $baseHeight
    $redstoneGraphics = New-Graphics $redstoneBitmap
    try {
        Draw-Crop $redstoneGraphics $baseBitmap 0 0 $baseBitmap.Width $baseBitmap.Height 0 0
        Draw-TabBackground $redstoneGraphics $tabRight 176 6 112 92
        Fill-TabPanelInterior $redstoneGraphics $tabRight 176 6 24 16 64 24
        Draw-HeaderIcon $redstoneGraphics $infoSignal 184 14 12 12
        Draw-Crop $redstoneGraphics $buttonDisabled 0 0 16 16 204 26
        Draw-Crop $redstoneGraphics $buttonEnabled 0 0 16 16 224 26
        Draw-Crop $redstoneGraphics $buttonDisabled 0 0 16 16 244 26
        Draw-Crop $redstoneGraphics $buttons 0 0 8 8 207 29
        Draw-Crop $redstoneGraphics $buttons 16 0 8 8 227 29
        Draw-Crop $redstoneGraphics $buttons 32 0 8 8 247 29
    } finally {
        $redstoneGraphics.Dispose()
    }
    Save-Bitmap $redstoneBitmap (Join-Path $OutputDir 'furnace_snapshot_redstone.png')
    Save-CroppedBitmap $redstoneBitmap 176 6 112 92 (Join-Path $OutputDir 'furnace_panel_redstone.png')

    $configWidth = 276
    $configBitmap = New-ArgbBitmap $configWidth $baseHeight
    $configGraphics = New-Graphics $configBitmap
    try {
        Draw-Crop $configGraphics $baseBitmap 0 0 $baseBitmap.Width $baseBitmap.Height 0 0
        Draw-TabBackground $configGraphics $tabRight 176 30 100 92
        Fill-TabPanelInterior $configGraphics $tabRight 176 30 16 20 64 64
        Draw-HeaderIcon $configGraphics $machineSide 184 38 12 12
        Draw-HeaderIcon $configGraphics $machineTop 216 54 16 16
        Draw-HeaderIcon $configGraphics $machineSide 196 74 16 16
        Draw-HeaderIcon $configGraphics $machineFace 216 74 16 16
        Draw-HeaderIcon $configGraphics $machineSide 236 74 16 16
        Draw-HeaderIcon $configGraphics $machineBottom 216 94 16 16
        Draw-HeaderIcon $configGraphics $machineSide 236 94 16 16
    } finally {
        $configGraphics.Dispose()
    }
    Save-Bitmap $configBitmap (Join-Path $OutputDir 'furnace_snapshot_config.png')
    Save-CroppedBitmap $configBitmap 176 30 100 92 (Join-Path $OutputDir 'furnace_panel_config.png')

    $augmentWidth = 276
    $augmentBitmap = New-ArgbBitmap $augmentWidth $baseHeight
    $augmentGraphics = New-Graphics $augmentBitmap
    try {
        Draw-Crop $augmentGraphics $baseBitmap 0 0 $baseBitmap.Width $baseBitmap.Height 0 0
        Draw-TabBackground $augmentGraphics $tabRight 176 54 100 92
        Fill-TabPanelInterior $augmentGraphics $tabRight 176 54 29 20 42 42
        Draw-HeaderIcon $augmentGraphics $slotGridAugment 184 62 12 12 0 0 18 18
        Draw-Crop $augmentGraphics $slotGridAugment 0 0 36 18 208 77
        Draw-Crop $augmentGraphics $slotGridAugment 0 0 36 18 208 95
    } finally {
        $augmentGraphics.Dispose()
    }
    Save-Bitmap $augmentBitmap (Join-Path $OutputDir 'furnace_snapshot_augment.png')
    Save-CroppedBitmap $augmentBitmap 176 54 100 92 (Join-Path $OutputDir 'furnace_panel_augment.png')
} finally {
    $furnaceBase.Dispose()
    $energy.Dispose()
    $slots.Dispose()
    $arrow.Dispose()
    $flame.Dispose()
    $tabRight.Dispose()
    $buttonEnabled.Dispose()
    $buttonDisabled.Dispose()
    $buttons.Dispose()
    $infoSignal.Dispose()
    $infoOutput.Dispose()
    $slotGridAugment.Dispose()
    $machineFace.Dispose()
    $machineSide.Dispose()
    $machineTop.Dispose()
    $machineBottom.Dispose()
    if ($null -ne $baseBitmap) { $baseBitmap.Dispose() }
    if ($null -ne $redstoneBitmap) { $redstoneBitmap.Dispose() }
    if ($null -ne $configBitmap) { $configBitmap.Dispose() }
    if ($null -ne $augmentBitmap) { $augmentBitmap.Dispose() }
}

Get-ChildItem -LiteralPath $OutputDir -Filter 'furnace_*.png' | Select-Object Name, Length

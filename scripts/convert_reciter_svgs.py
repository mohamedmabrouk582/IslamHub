#!/usr/bin/env python3
"""Convert reciter PNG sources to vector SVG assets for the app."""
from __future__ import annotations

import argparse
from pathlib import Path

import vtracer


def convert_directory(input_dir: Path, output_dir: Path) -> None:
    output_dir.mkdir(parents=True, exist_ok=True)
    for png in sorted(input_dir.glob("reciter_*.png")):
        svg_path = output_dir / f"{png.stem}.svg"
        vtracer.convert_image_to_svg_py(str(png), str(svg_path))
        print(f"Converted {png.name} -> {svg_path.name}")


def main() -> None:
    parser = argparse.ArgumentParser()
    parser.add_argument(
        "--input",
        type=Path,
        default=Path("assets/reciter_sources"),
        help="Folder containing reciter_*.png source images",
    )
    parser.add_argument(
        "--output",
        type=Path,
        default=Path("app/src/main/assets/reciters"),
        help="Output folder for SVG assets",
    )
    args = parser.parse_args()
    convert_directory(args.input, args.output)


if __name__ == "__main__":
    main()

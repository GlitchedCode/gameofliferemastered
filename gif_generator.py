import glob
import os
import sys
from PIL import Image
from collections import namedtuple
import numpy as np


def load_palette(path):
    """
    Reads file at path, and returns a bytearray palette.
    Each color should be on a separate line, and components should be separated by a comma.
    """
    f = open(path, "r")
    lines = f.readlines()
    colors = []

    for line in lines:
        for comp in line.split(","):
            colors.append(int(comp))

    return bytearray(colors)


def trim_frames(frames):
    left = frames[0].size[0] / 2
    upper = frames[0].size[1] / 2
    right = frames[0].size[0] / 2
    lower = frames[0].size[1] / 2
    ret = []

    for frame in frames:
        bbox = frame.getbbox()
        try:
            if bbox[0] < left:
                left = bbox[0]
            if bbox[1] < upper:
                upper = bbox[1]
            if bbox[2] > right:
                right = bbox[2]
            if bbox[3] > lower:
                lower = bbox[3]
        except:
            pass

    for frame in frames:
        if frame.getbbox():
            ret.append(frame.crop((left, upper, right, lower)))

    return ret


def make_gif(palette, frames, output):
    frame_one = frames[0]
    frame_one.save(f"{output}.gif", format="GIF", append_images=frames, optimize=True,
                   save_all=True, duration=75, loop=0)#, palette=frame_one.palette)


if __name__ == "__main__":
    from sys import argv
    argc = len(argv)
    if argc < 3:
        print("usage: make_gif.py <dir> <output_basename>")
        print("dir: directory containing PNG files used as frames")
        print("output_basename: base name for output files, should not contain extensions")
        exit(1)

    folder = argv[1]
    basename = argv[2]

    files = sorted(glob.glob(f"{folder}/*.gif"), key=os.path.getmtime)
    frames = [Image.open(image) for image in files]
    palette = load_palette(f"{folder}/palette.txt")

    frames = trim_frames(frames)
    make_gif(palette, frames, basename)

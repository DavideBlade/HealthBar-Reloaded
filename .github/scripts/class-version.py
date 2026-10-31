"""
Finds the Java version a jar needs: the highest class file version among its classes (own and shaded).
Prints how many classes need each Java version, then "class-version=<version>" for GitHub Actions.
Usage: python3 class-version.py <jar>
"""
import sys
import zipfile
from collections import Counter

counts = Counter()
with zipfile.ZipFile(sys.argv[1]) as jar:
    for name in jar.namelist():
        # Classes under META-INF/versions/ are loaded only by newer Java versions (multi-release jars)
        if not name.endswith(".class") or name.startswith("META-INF/versions/"):
            continue
        header = jar.read(name)[:8]
        if header[:4] != b"\xca\xfe\xba\xbe":
            sys.exit(f"{name} is not a class file")
        counts[int.from_bytes(header[6:8], "big")] += 1

if not counts:
    sys.exit("No classes in the jar")

for version, count in sorted(counts.items()):
    print(f"Class version {version} (Java {version - 44}): {count} classes", file=sys.stderr)
print(f"class-version={max(counts)}")

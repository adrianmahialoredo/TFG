"""Small console helpers shared by the data insertion scripts.

Windows consoles default to a legacy code page (cp1252 in Spanish locales),
which makes ``print`` raise ``UnicodeEncodeError`` as soon as a non latin-1
character such as ``✓`` or ``✗`` is written. The insertion scripts use those
symbols for their progress output, so they must force UTF-8 on stdout/stderr
before printing anything.
"""

import sys


def configure_console():
    """Force UTF-8 output so progress symbols never crash the script.

    Falls back silently on interpreters/streams that do not support
    ``reconfigure`` (Python < 3.7 or redirected non-text streams).
    """
    for stream in (sys.stdout, sys.stderr):
        try:
            stream.reconfigure(encoding='utf-8', errors='replace')
        except (AttributeError, ValueError, OSError):
            pass


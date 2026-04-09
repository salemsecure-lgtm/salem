#!/usr/bin/env python3
"""
Compact deck builder — takes a deck spec dict and produces a PPTX.
"""
import sys, os
sys.path.insert(0, os.path.dirname(__file__))
from pem_design_system import *

def build_deck(spec, output_path):
    """Build a complete deck from a specification dict."""
    prs = create_presentation()
    dn = spec['number']
    st = spec['short_title']
    sn = 1  # slide counter

    # Title slide
    add_title_slide(prs, dn, spec['title'], spec['subtitle'], st)
    sn += 1

    # Objectives
    add_objectives_slide(prs, spec['objectives'], dn, st, sn)
    sn += 1

    # All body slides
    for s in spec['slides']:
        t = s['type']
        notes = s.get('notes', '')
        if t == 'content':
            add_content_slide(prs, s['title'], s['bullets'], notes, dn, st, sn,
                            s.get('accent', TEAL))
        elif t == 'two_column':
            add_two_column_slide(prs, s['title'], s['left_title'], s['left'],
                               s['right_title'], s['right'], notes, dn, st, sn,
                               s.get('left_color', TEAL), s.get('right_color', CORAL))
        elif t == 'key_point':
            add_key_point_slide(prs, s['title'], s['point'], s.get('sub', ''),
                              notes, dn, st, sn)
        elif t == 'table':
            add_table_slide(prs, s['title'], s['headers'], s['rows'], notes, dn, st, sn)
        elif t == 'algorithm':
            add_algorithm_slide(prs, s['title'], s['steps'], notes, dn, st, sn)
        elif t == 'case':
            add_case_slide(prs, s['title'], s['scenario'], s.get('questions'),
                         notes, dn, st, sn)
        elif t == 'pitfalls':
            add_pitfalls_slide(prs, s['title'], s['items'], notes, dn, st, sn)
        elif t == 'section':
            add_section_divider(prs, s['title'], s.get('subtitle', ''), dn, st)
        sn += 1

    # Takeaways
    add_takeaway_slide(prs, spec['takeaways'], dn, st, sn)
    sn += 1

    # References
    add_references_slide(prs, spec['references'], dn, st, sn)
    sn += 1

    # End slide
    add_end_slide(prs, dn, st)

    save_deck(prs, output_path)
    print(f"  Created: {output_path} ({sn+1} slides)")
    return output_path

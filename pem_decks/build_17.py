#!/usr/bin/env python3
"""Build Deck 17: Electrolyte Emergencies in PEM"""
import sys, os
sys.path.insert(0, os.path.dirname(__file__))
from build_deck import build_deck

spec = {
    'number': 17,
    'short_title': 'Electrolyte Emergencies',
    'title': 'Electrolyte Emergencies in Pediatric Emergency Medicine',
    'subtitle': 'Hypo/Hypernatremia, Hypo/Hyperkalemia, Hypocalcemia, and Acute Correction Protocols',
    'objectives': [
        'Identify life-threatening electrolyte derangements and their clinical presentations',
        'Calculate sodium correction rates to avoid osmotic demyelination and cerebral edema',
        'Manage hyperkalemia emergently with membrane stabilization, shifting, and elimination',
        'Recognize symptomatic hypocalcemia and treat appropriately',
        'Understand age-specific electrolyte normal values and common pediatric etiologies',
    ],
    'slides': [
        {'type': 'content', 'title': 'Electrolyte Emergencies in PEM: What Kills and How Fast',
         'bullets': [
             'Hyperkalemia: cardiac arrest within minutes — the most immediately lethal electrolyte emergency',
             'Hyponatremia: seizures and cerebral edema — dangerous when acute (<48h) or corrected too fast',
             'Hypernatremia: cerebral hemorrhage if corrected too rapidly',
             'Hypocalcemia: seizures, QT prolongation, cardiac arrest',
             'Hypomagnesemia: refractory hypocalcemia and hypokalemia',
             '>Rule: if the patient is seizing, crashing, or coding — check the electrolytes NOW',
         ],
         'notes': 'Frame this talk around urgency. Electrolyte emergencies kill by two mechanisms: the derangement itself (hyperkalemia causing cardiac arrest) or the correction (too-rapid sodium correction causing osmotic demyelination). Both are preventable. The PEM physician must know the emergency treatments cold.'},

        {'type': 'section', 'title': 'Sodium Emergencies', 'subtitle': 'Hyponatremia and Hypernatremia'},

        {'type': 'table', 'title': 'Hyponatremia: Classification and Management',
         'headers': ['Severity', 'Sodium Level', 'Symptoms', 'Management'],
         'rows': [
             ['Mild', '130-134', 'Often asymptomatic', 'Fluid restriction, treat underlying cause'],
             ['Moderate', '125-129', 'Nausea, headache, malaise', 'Careful correction, investigate cause'],
             ['Severe', '<125', 'Seizures, AMS, coma, herniation', 'EMERGENT 3% NaCl 3-5 mL/kg IV bolus'],
         ],
         'notes': 'Symptomatic hyponatremia (seizures, AMS) is a medical emergency. Treatment is 3% hypertonic saline 3-5 mL/kg IV over 10-20 minutes. This raises Na by ~3-5 mEq/L and should stop the seizures. After the acute correction, slow down: maximum correction rate 10-12 mEq/L in 24 hours to avoid osmotic demyelination syndrome. If the hyponatremia developed over >48 hours, correct even more slowly (8 mEq/L/24h).'},

        {'type': 'algorithm', 'title': 'Hypernatremia Correction Protocol',
         'steps': [
             'ASSESS: dehydration (most common cause in children) vs. excess Na (rare)',
             'CALCULATE free water deficit: 4 mL/kg × (current Na - desired Na)',
             'CORRECTION RATE: maximum 10-12 mEq/L per 24 hours (0.5 mEq/L/hr)',
             'FLUID CHOICE: D5 0.2-0.45% NS (hypotonic relative to patient); avoid free water boluses',
             'MONITOR: Na q2-4h initially; adjust rate based on actual correction speed',
             'Too-rapid correction → cerebral edema (brain cells adapted to hypertonicity)',
         ],
         'notes': 'Hypernatremia in children is almost always due to dehydration (gastroenteritis, poor intake). The brain adapts to hypertonicity by generating idiogenic osmoles. If you correct sodium too rapidly, water rushes into brain cells and causes cerebral edema. The correction must be slow and monitored. Check Na every 2-4 hours and adjust fluids accordingly.'},

        {'type': 'section', 'title': 'Potassium Emergencies', 'subtitle': 'Hyperkalemia — the silent cardiac killer'},

        {'type': 'content', 'title': 'Hyperkalemia: Recognition and Emergency Treatment',
         'bullets': [
             'Normal K: 3.5-5.0 mEq/L (higher in neonates: up to 6.0)',
             'Causes in children: renal failure, DKA (redistribution), tumor lysis, adrenal insufficiency, hemolyzed sample',
             'ECG changes (progressive): peaked T waves → PR prolongation → wide QRS → sine wave → cardiac arrest',
             'CRITICAL: K >7.0 or any ECG changes = treat immediately',
             'Step 1 — STABILIZE membrane: calcium gluconate 100 mg/kg IV over 5 min (max 3g)',
             'Step 2 — SHIFT K into cells: insulin 0.1 U/kg + D25W 2 mL/kg; albuterol neb 2.5-5 mg',
             'Step 3 — ELIMINATE K: sodium polystyrene (Kayexalate), furosemide, dialysis if refractory',
             '>If you see peaked T waves on the monitor, give calcium FIRST — it is the fastest-acting agent',
         ],
         'notes': 'This is the most time-critical electrolyte emergency. Calcium gluconate does NOT lower potassium — it stabilizes the cardiac membrane to prevent arrhythmia. It buys you time while you implement the shifting and elimination strategies. Insulin + glucose is the most effective shifting agent. Always check if the sample is hemolyzed — false hyperkalemia from hemolysis is very common in pediatric blood draws.'},

        {'type': 'table', 'title': 'Hyperkalemia: Treatment Quick Reference',
         'headers': ['Agent', 'Mechanism', 'Dose', 'Onset', 'Duration'],
         'rows': [
             ['Calcium gluconate 10%', 'Membrane stabilization', '100 mg/kg (1 mL/kg) IV over 5 min', '1-3 min', '30-60 min'],
             ['Regular insulin + D25W', 'Shift K intracellular', 'Insulin 0.1 U/kg + D25W 2 mL/kg', '15-30 min', '4-6 h'],
             ['Albuterol nebulized', 'Shift K intracellular', '2.5 mg (<25 kg) or 5 mg (>25 kg)', '15-30 min', '2-4 h'],
             ['Sodium bicarbonate', 'Shift K (if acidotic)', '1-2 mEq/kg IV', '15-30 min', '1-2 h'],
             ['Furosemide', 'Renal K elimination', '1-2 mg/kg IV', '30-60 min', '6 h'],
             ['Kayexalate', 'GI K elimination', '1 g/kg PO/PR', '1-2 h', 'Hours'],
             ['Dialysis', 'Definitive elimination', 'Contact nephrology', 'Immediate (once initiated)', 'During session'],
         ],
         'notes': 'Key sequence: STABILIZE (calcium) → SHIFT (insulin/albuterol) → ELIMINATE (furosemide/dialysis). Calcium gluconate is preferred over calcium chloride in children because it is less irritating to veins and has lower risk of tissue necrosis if extravasated. Kayexalate onset is slow and uncertain — do not rely on it as your primary treatment in acute hyperkalemia.'},

        {'type': 'content', 'title': 'Hypocalcemia: Causes, Recognition, and Treatment',
         'bullets': [
             'Normal ionized Ca: 1.1-1.3 mmol/L (use ionized, not total calcium)',
             'Common causes in children: DiGeorge syndrome, hypoparathyroidism, vitamin D deficiency, critical illness, citrate toxicity (massive transfusion)',
             'Neonatal hypocalcemia: early (first 48h, associated with prematurity) or late (day 5-10, associated with high-phosphorus formula)',
             'Symptoms: tetany, Chvostek sign, Trousseau sign, seizures, QT prolongation, laryngospasm',
             'Severe/symptomatic: calcium gluconate 10% 100 mg/kg (1 mL/kg) IV over 10 min with cardiac monitoring',
             'QT prolongation + hypocalcemia = risk of Torsades de Pointes',
             '>Always check magnesium when treating hypocalcemia — hypomagnesemia causes refractory hypocalcemia',
         ],
         'notes': 'The critical pearl: hypocalcemia cannot be corrected if magnesium is also low. Magnesium is required for PTH secretion. If you are giving calcium and it is not correcting, check and replace magnesium first. In the setting of massive transfusion, citrate in blood products binds calcium — give empiric calcium with every 3-4 units of blood products.'},

        {'type': 'case', 'title': '2-Year-Old with Seizures and Na of 118',
         'scenario': 'A 2-year-old boy (13 kg) presents with a generalized seizure lasting 3 minutes. He had gastroenteritis for 3 days with vomiting and diarrhea. Parents have been giving diluted formula and water to "keep him hydrated." He is post-ictal, lethargic. Labs: Na 118, K 3.2, Cl 88, CO2 14, BUN 18, Cr 0.4, glucose 95.',
         'questions': [
             'What caused the seizure? (Acute hyponatremia from water intoxication — diluted formula)',
             'Immediate treatment? (3% NaCl 3 mL/kg = 39 mL IV over 15 min → raises Na ~3-5 mEq/L)',
             'Target for first 4-6 hours? (Raise Na to ~122-125 to stop seizure activity)',
             'Maximum 24-hour correction? (10-12 mEq/L — so target Na ~128-130 by 24 hours)',
             'If Na corrects too fast? → D5W infusion to slow the rise; desmopressin if needed',
         ],
         'notes': 'Classic PEM case of water intoxication from diluted formula. This is preventable with parent education. The treatment is straightforward: 3% NaCl bolus to stop the seizures, then slow correction. Calculate correction carefully. If Na rises too fast (>12 mEq/L in 24h), you can give free water (D5W) or desmopressin to slow the correction and prevent osmotic demyelination.'},

        {'type': 'pitfalls', 'title': 'Electrolyte Emergency Pitfalls',
         'items': [
             'Correcting hyponatremia too rapidly → osmotic demyelination syndrome (max 10-12 mEq/L per 24h)',
             'Correcting hypernatremia too rapidly → cerebral edema (max 10-12 mEq/L per 24h)',
             'Not giving calcium FIRST in hyperkalemia with ECG changes — it is the fastest-acting agent',
             'Treating a hemolyzed K level as real hyperkalemia — always confirm before aggressive treatment',
             'Forgetting to check magnesium with refractory hypocalcemia or hypokalemia',
             'Using total calcium instead of ionized calcium — total Ca is unreliable in critically ill children',
             'Not monitoring Na frequently enough during correction — check q2-4h and adjust fluids accordingly',
         ],
         'notes': 'The overcorrection pitfalls are the most important. Both too-rapid correction of hyponatremia and hypernatremia are iatrogenic disasters. Set a correction rate, calculate the expected change with your chosen fluid, check electrolytes frequently, and adjust. The hemolyzed sample issue is very common in pediatrics due to difficult blood draws.'},
    ],
    'takeaways': [
        'Hyperkalemia with ECG changes: calcium gluconate FIRST (membrane stabilization), then insulin + glucose + albuterol',
        'Symptomatic hyponatremia (seizures): 3% NaCl 3-5 mL/kg IV bolus; max correction 10-12 mEq/L per 24h',
        'Hypernatremia: correct slowly (max 10-12 mEq/L per 24h) with hypotonic fluids; monitor Na q2-4h',
        'Always check ionized calcium (not total) and magnesium when treating electrolyte emergencies',
        'Before treating hyperkalemia — confirm the sample is not hemolyzed',
    ],
    'references': [
        'Moritz ML, Ayus JC. Prevention of hospital-acquired hyponatremia: do we have the answers? Pediatrics. 2011;128(5):980-983.',
        'Sterns RH. Disorders of plasma sodium — causes, consequences, and correction. N Engl J Med. 2015;372(1):55-65.',
        'Mahoney BA, et al. Emergency interventions for hyperkalaemia. Cochrane Database Syst Rev. 2005;2:CD003235.',
        'Greenbaum LA. Electrolyte and acid-base disorders. In: Kliegman RM, ed. Nelson Textbook of Pediatrics. 21st ed. Elsevier; 2020.',
        'Feld LG, et al. Clinical practice guideline: maintenance intravenous fluids in children. Pediatrics. 2018;142(6):e20183083.',
    ],
}

if __name__ == '__main__':
    build_deck(spec, os.path.join(os.path.dirname(__file__), '17_electrolyte_emergencies.pptx'))

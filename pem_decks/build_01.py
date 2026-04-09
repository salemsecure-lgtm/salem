#!/usr/bin/env python3
"""Build Deck 1: Approach to the Critically Ill Child in the ED"""
import sys, os
sys.path.insert(0, os.path.dirname(__file__))
from build_deck import build_deck
from pem_design_system import TEAL, CORAL, NAVY, AMBER

spec = {
    'number': 1,
    'short_title': 'Critically Ill Child',
    'title': 'Approach to the Critically Ill Child in the ED',
    'subtitle': 'Systematic Recognition, Stabilization, and Resuscitation in Pediatric Emergency Medicine',
    'objectives': [
        'Recognize the critically ill child within 15 seconds using the Pediatric Assessment Triangle',
        'Apply the structured ABCDE approach to pediatric stabilization',
        'Differentiate compensated from decompensated physiologic states',
        'Implement team-based resuscitation principles in pediatric emergencies',
        'Identify common cognitive errors that delay recognition of critical illness',
    ],
    'slides': [
        # Why this matters
        {'type': 'content', 'title': 'Why This Matters in PEM',
         'bullets': [
             'Children compensate until they crash — the window to intervene is narrow',
             'Pediatric cardiac arrest survival with good neurologic outcome: ~10-15%',
             'Most pediatric arrests are respiratory in origin — early recognition prevents arrest',
             'The first 5 minutes of your assessment determine the trajectory',
             '>Delayed recognition of critical illness is the #1 preventable error in PEM',
         ],
         'notes': 'Open with this to frame urgency. Unlike adults where cardiac arrest is often primary, children almost always arrest from respiratory failure or shock. This means we have a window to intervene — but only if we recognize the problem early. Stress that the goal is to identify and treat BEFORE arrest.'},

        # PAT
        {'type': 'content', 'title': 'The Pediatric Assessment Triangle (PAT)',
         'bullets': [
             'The 15-second "across the room" assessment — no stethoscope needed',
             'APPEARANCE: tone, interactiveness, consolability, look/gaze, speech/cry (TICLS)',
             '>Abnormal appearance is the most sensitive indicator of critical illness',
             'WORK OF BREATHING: nasal flaring, retractions, head bobbing, audible sounds, positioning',
             'CIRCULATION TO SKIN: pallor, mottling, cyanosis, delayed cap refill visible from doorway',
             'Abnormality in any ONE component = potentially critical — act now',
         ],
         'notes': 'The PAT is the single most important tool in your PEM arsenal. Teach it as a doorway assessment. Appearance is the most sensitive for serious illness. An abnormal PAT means you move the child to resuscitation immediately. Practice this on every patient — not just the sick ones.'},

        # PAT Table
        {'type': 'table', 'title': 'PAT Interpretation — Pattern Recognition',
         'headers': ['PAT Pattern', 'Appearance', 'Breathing', 'Circulation', 'Likely Category'],
         'rows': [
             ['Stable', 'Normal', 'Normal', 'Normal', 'Non-urgent'],
             ['Resp Distress', 'Normal', 'Abnormal', 'Normal', 'Primary respiratory'],
             ['Resp Failure', 'Abnormal', 'Abnormal', 'Normal', 'Respiratory → intervene now'],
             ['Shock (comp)', 'Normal', 'Normal', 'Abnormal', 'Compensated shock'],
             ['Shock (decomp)', 'Abnormal', 'Normal', 'Abnormal', 'Decompensated shock'],
             ['CNS/Metabolic', 'Abnormal', 'Normal', 'Normal', 'Neuro or metabolic'],
             ['Cardiopulm Fail', 'Abnormal', 'Abnormal', 'Abnormal', 'Imminent arrest'],
         ],
         'notes': 'This table is gold for pattern recognition. Walk through each row and give a clinical example. Cardiopulmonary failure with all three abnormal means arrest is imminent — this child needs immediate intervention. The beauty of PAT is it works without touching the patient.'},

        # First 5 minutes
        {'type': 'algorithm', 'title': 'The First 5 Minutes: ABCDE Framework',
         'steps': [
             'A — Airway: position, suction, adjuncts; is it patent and maintainable?',
             'B — Breathing: RR, SpO2, auscultation; give O2 if any concern',
             'C — Circulation: HR, pulses, cap refill, BP; establish IV/IO access',
             'D — Disability: AVPU, pupils, glucose — CHECK GLUCOSE EVERY TIME',
             'E — Exposure: fully undress, temp, head-to-toe; prevent hypothermia',
         ],
         'notes': 'Walk through ABCDE systematically. The key teaching point is that you stabilize each letter before moving to the next. Airway always comes first. Glucose is highlighted under D because it is the single most commonly missed reversible cause of altered mental status in children. Make it a reflex.'},

        # Vital signs by age
        {'type': 'table', 'title': 'Normal Vital Signs by Age — Know Your Norms',
         'headers': ['Age', 'Heart Rate', 'Respiratory Rate', 'Systolic BP (5th %ile)', 'Min SBP Alert'],
         'rows': [
             ['Neonate', '120-160', '30-60', '60-70', '<60'],
             ['Infant (1-12mo)', '100-150', '25-40', '70-80', '<70'],
             ['Toddler (1-3y)', '90-140', '20-30', '80-90', '<70 + (2×age)'],
             ['Preschool (4-5y)', '80-120', '20-25', '85-95', '<70 + (2×age)'],
             ['School-age (6-12y)', '70-110', '15-20', '90-105', '<70 + (2×age)'],
             ['Adolescent (>12y)', '60-100', '12-18', '100-120', '<90'],
         ],
         'notes': 'This is a slide you will reference constantly. The minimum SBP formula for children 1-10 years is 70 + (2 × age in years). Remember that hypotension is a LATE sign — by the time BP drops, the child has lost 25-30% blood volume. Tachycardia is the earliest vital sign change in shock.'},

        # Compensated vs decompensated
        {'type': 'two_column', 'title': 'Compensated vs Decompensated: The Critical Distinction',
         'left_title': 'COMPENSATED (Act Now)',
         'left': [
             'Tachycardia (earliest sign)',
             'Normal blood pressure',
             'Altered skin perfusion (cool, mottled)',
             'Prolonged capillary refill (>2-3 sec)',
             'Irritable or anxious (early)',
             'Decreased urine output',
             'Narrowed pulse pressure',
             'STILL RESPONSIVE TO FLUIDS/INTERVENTION',
         ],
         'right_title': 'DECOMPENSATED (Near Arrest)',
         'right': [
             'Bradycardia (ominous in children)',
             'Hypotension (LATE — 25-30% volume loss)',
             'Mottling, pallor, cyanosis',
             'Absent peripheral pulses',
             'Lethargic, obtunded, unresponsive',
             'Anuria',
             'Absent capillary refill',
             'ARREST IS IMMINENT — escalate NOW',
         ],
         'notes': 'This is the most critical teaching slide. Hammer home that children compensate beautifully until they crash. Hypotension means the child has exhausted all compensatory mechanisms. Bradycardia in a child is a pre-arrest rhythm — never reassuring. Your job is to catch compensated shock and treat before decompensation.'},

        # Clinical gestalt
        {'type': 'key_point', 'title': 'Clinical Pearl',
         'point': '"This child looks sick" is a valid clinical assessment.',
         'sub': 'The experienced PEM provider\'s gestalt has been shown to correlate with illness severity. But back it up with systematic assessment. Gestalt + PAT + ABCDE = comprehensive recognition.',
         'notes': 'This validates clinical experience while reinforcing systematic approaches. Studies show that physician gestalt "this child looks sick" has reasonable sensitivity for serious illness but needs to be paired with objective assessment to avoid anchoring bias and premature closure.'},

        # Section: Management priorities
        {'type': 'section', 'title': 'Stabilization\nPriorities', 'subtitle': 'Airway, breathing, circulation interventions in the first critical minutes'},

        # Airway priorities
        {'type': 'content', 'title': 'Airway: Pediatric-Specific Priorities',
         'bullets': [
             'Position of comfort — do not force supine if child is maintaining own airway',
             'Sniffing position for infants; head-tilt chin-lift for older children',
             'Jaw thrust if cervical spine concern',
             'Suction: use Yankauer or bulb; limit to 10 seconds per attempt',
             'Oropharyngeal airway (OPA): only in unconscious patients — measure corner of mouth to angle of jaw',
             'Nasopharyngeal airway (NPA): tolerated in semiconscious patients',
             '>Do NOT attempt advanced airway until BVM ventilation fails or is inadequate',
         ],
         'notes': 'Emphasize that most pediatric airways can be managed with positioning and BVM. The urge to intubate should be tempered by the reality that pediatric intubation has higher complication rates than adult intubation. A child who is maintaining their airway in tripod position should not be laid flat.'},

        # Breathing interventions
        {'type': 'content', 'title': 'Breathing: Rapid Assessment and Intervention',
         'bullets': [
             'Apply SpO2 immediately — target ≥94% in most situations',
             'High-flow O2 via non-rebreather if any concern',
             'Auscultate: symmetry, air entry, adventitious sounds',
             'BVM ventilation: 15-20 breaths/min (infant), 10-12 (child), avoid hyperventilation',
             'Consider HFNC for persistent work of breathing (flow 1-2 L/kg/min)',
             '>Key: "Low and slow" with BVM — avoid gastric distension and barotrauma',
             'Tension pneumothorax: needle decompression 2nd ICS MCL if absent breath sounds + shock',
         ],
         'notes': 'The most common BVM error is hyperventilation — it increases intrathoracic pressure, reduces venous return, and worsens hemodynamics. Coach your team to use the slow squeeze-release-release technique. HFNC has revolutionized PEM management of respiratory distress and should be available in every resuscitation bay.'},

        # Circulation interventions
        {'type': 'content', 'title': 'Circulation: Access, Fluids, and Beyond',
         'bullets': [
             'Two attempts at peripheral IV — then go INTRAOSSEOUS (IO)',
             'IO sites: proximal tibia (preferred), distal femur, distal tibia, humeral head',
             'IO works for all resuscitation drugs and fluids — do not delay',
             'Fluid bolus: 20 mL/kg isotonic crystalloid (NS or LR) over 5-20 minutes',
             'Reassess after each bolus — repeat up to 60 mL/kg in first hour if needed',
             'If fluid-refractory (no improvement after 40-60 mL/kg): START VASOPRESSORS',
             '>Epinephrine 0.1-0.3 mcg/kg/min for cold shock; Norepinephrine for warm shock',
         ],
         'notes': 'The IO is your best friend in pediatric resuscitation. Two failed PIV attempts and 90 seconds is the standard threshold — then drill. Push fluids rapidly in hemorrhagic and septic shock, but be cautious in cardiogenic shock where fluid overload worsens the situation. After 40-60 mL/kg of crystalloid, if the child is not improving, you need pressors and likely PICU.'},

        # Disability
        {'type': 'content', 'title': 'Disability: Neuro and Glucose',
         'bullets': [
             'AVPU: Alert, Voice responsive, Pain responsive, Unresponsive',
             'GCS: use modified pediatric GCS for pre-verbal children',
             'Pupils: size, reactivity, symmetry — fixed dilated = herniation until proven otherwise',
             'CHECK GLUCOSE: hypoglycemia is reversible and commonly missed',
             '>Glucose targets: neonate >45 mg/dL, child >60 mg/dL',
             '>Treatment: D10W 5 mL/kg (neonates), D25W 2-4 mL/kg (children)',
             'Posturing: decorticate (flexion) = cortical; decerebrate (extension) = brainstem',
         ],
         'notes': 'If you take one thing from this deck, make it this: ALWAYS check glucose in the critically ill child. It takes 5 seconds and can be immediately life-saving. Hypoglycemia mimics nearly every neurologic emergency and is completely reversible. Use D10W in neonates to avoid osmotic injury.'},

        # Escalation triggers
        {'type': 'pitfalls', 'title': 'When to Escalate — Call for Help Early',
         'items': [
             'Fluid-refractory shock (no improvement after 40-60 mL/kg) → PICU, pressors',
             'Impending respiratory failure despite HFNC/NIV → prepare for intubation',
             'GCS ≤8 or declining neurologic exam → intubate, neuroprotection',
             'Persistent bradycardia despite oxygenation → epinephrine, prepare for arrest',
             'Unknown etiology with worsening trajectory → call senior/attending NOW',
             'Any child requiring >2 fluid boluses → activate critical care response',
         ],
         'notes': 'Fellows often hesitate to call for help. Reframe this: calling early is a sign of competence, not weakness. The PICU team would rather be called early for a child they can stabilize than late for a child in full arrest. Create a low threshold for activating additional resources.'},

        # Team dynamics
        {'type': 'content', 'title': 'Team-Based Resuscitation: CRM Principles',
         'bullets': [
             'Assign a TEAM LEADER: directs care, maintains situational awareness',
             'Closed-loop communication: order → repeat back → confirm completion',
             'Role assignment: airway, access/meds, compressions, recorder, family liaison',
             'Use cognitive aids: PEM resuscitation cards, Broselow tape, crisis checklists',
             'Limit interruptions to the team leader — one clear voice giving orders',
             'Debrief after every resuscitation — hot debrief within 30 minutes',
             '>The team leader should NOT be performing procedures',
         ],
         'notes': 'Crew Resource Management from aviation saves lives in medicine. The team leader stands at the foot of the bed, oversees everything, and does not get pulled into tasks. Closed-loop communication prevents errors. Debriefing is critical for team learning and emotional processing. Make it a habit after every resuscitation.'},

        # Cognitive errors
        {'type': 'pitfalls', 'title': 'Cognitive Errors That Kill in Pediatric Resuscitation',
         'items': [
             'Anchoring: locking onto first diagnosis and ignoring contradictory data',
             'Premature closure: stopping the workup too early ("it\'s just a virus")',
             'Normalcy bias: assuming tachycardia is from crying/fever — may be shock',
             'Fixation error: tunnel vision on the airway while the child bleeds',
             'Failure to reassess: initial plan not working but no pivot made',
             'Weight estimation errors: always use Broselow or actual weight for drug dosing',
         ],
         'notes': 'These cognitive biases are well-described in emergency medicine literature. The antidote is systematic assessment with forced reassessment at defined intervals. Encourage the team to challenge assumptions. A diagnostic time-out every 5-10 minutes during resuscitation helps break anchoring.'},

        # Clinical case
        {'type': 'case', 'title': 'A Lethargic Toddler with Mottled Skin',
         'scenario': 'A 2-year-old boy is brought in by EMS. Parents report 2 days of fever, decreased PO intake, and progressive lethargy. On arrival: PAT shows abnormal appearance (limp, not interactive), normal work of breathing, abnormal circulation (mottled, pale). HR 185, RR 32, BP 75/40, SpO2 97% on room air, Temp 39.8°C, cap refill 5 seconds, GCS 10 (E2 V3 M5).',
         'questions': [
             'What does the PAT tell you? (Decompensated shock — 2 of 3 abnormal)',
             'What are your first three actions? (IV/IO access, 20 mL/kg NS bolus, glucose check)',
             'After 40 mL/kg NS, HR 175, cap refill still 4 sec — next step? (Start vasopressors, call PICU)',
             'What is the most likely etiology? (Septic shock — empiric antibiotics NOW)',
         ],
         'notes': 'Walk through this case interactively. Let the audience identify the PAT pattern first. This is classic decompensated septic shock. Key teaching: the BP is technically "normal" for age but with other signs of poor perfusion, this child is in shock. Do not be falsely reassured by a normal BP in a child with tachycardia and poor perfusion.'},

        # Case debrief
        {'type': 'content', 'title': 'Case Debrief: Key Decision Points',
         'bullets': [
             'PAT identified decompensated shock in 15 seconds — move to resus immediately',
             'IV/IO access within 5 minutes; if PIV fails twice, go IO without hesitation',
             'Glucose was 42 — gave D25W 2 mL/kg; this alone improved GCS to 12',
             'After 60 mL/kg crystalloid: still tachycardic → started epinephrine drip (0.1 mcg/kg/min)',
             'Blood cultures drawn, empiric ceftriaxone 100 mg/kg + vancomycin started',
             'Admitted to PICU; blood culture grew Streptococcus pneumoniae at 12 hours',
         ],
         'notes': 'Debrief the case by highlighting the critical decision points. The glucose check changing management is a powerful teaching moment. Emphasize that fluid-refractory shock requires vasopressors — do not keep giving fluid beyond 60 mL/kg hoping it will work. This child survived with good outcome because of rapid, systematic management.'},
    ],
    'takeaways': [
        'Use the PAT (Pediatric Assessment Triangle) on EVERY patient — it takes 15 seconds',
        'Hypotension is a LATE sign in children — recognize and treat compensated shock',
        'Check glucose in every critically ill child — it is reversible and commonly missed',
        'Two failed PIV attempts → go IO immediately; do not delay resuscitation for access',
        'Call for help early — escalation is a sign of good clinical judgment, not weakness',
    ],
    'references': [
        'American Heart Association. PALS Provider Manual, 2020 Guidelines Update.',
        'Dieckmann RA, Brownstein D, Gausche-Hill M. The Pediatric Assessment Triangle: a novel approach for the rapid evaluation of children. Pediatr Emerg Care. 2010;26(4):312-315.',
        'Davis AL, Carcillo JA, et al. American College of Critical Care Medicine Clinical Practice Parameters for Hemodynamic Support of Pediatric and Neonatal Septic Shock. Crit Care Med. 2017;45(6):1061-1093.',
        'Topjian AA, Raymond TT, et al. Part 4: Pediatric Basic and Advanced Life Support. 2020 AHA Guidelines. Circulation. 2020;142(16_suppl_2):S469-S523.',
        'Croskerry P. The importance of cognitive errors in diagnosis and strategies to minimize them. Acad Med. 2003;78(8):775-780.',
        'Fleisher GR, Ludwig S. Textbook of Pediatric Emergency Medicine, 7th Edition. Wolters Kluwer, 2020.',
    ],
}

if __name__ == '__main__':
    build_deck(spec, os.path.join(os.path.dirname(__file__), '01_critically_ill_child_ed.pptx'))

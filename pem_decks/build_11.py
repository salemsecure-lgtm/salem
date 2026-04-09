#!/usr/bin/env python3
"""Build Deck 11: Pediatric Analgesia in the ED"""
import sys, os
sys.path.insert(0, os.path.dirname(__file__))
from build_deck import build_deck

spec = {
    'number': 11,
    'short_title': 'Pediatric Analgesia',
    'title': 'Pediatric Analgesia in the ED',
    'subtitle': 'Multimodal Pain Management, Regional Anesthesia, and Opioid-Sparing Strategies',
    'objectives': [
        'Assess pain accurately in children across all developmental stages',
        'Build a multimodal analgesia plan tailored to pain type and severity',
        'Perform common ED nerve blocks: femoral, fascia iliaca, digital, hematoma block',
        'Apply opioid-sparing strategies without undertreating pain',
        'Manage procedural pain and distress using non-pharmacologic and pharmacologic approaches',
    ],
    'slides': [
        {'type': 'content', 'title': 'The Problem: Oligoanalgesia in Pediatric Emergency Medicine',
         'bullets': [
             'Children receive less analgesia than adults for the same conditions — well-documented disparity',
             'Barriers: fear of opioids in children, difficulty assessing pain, "they will cry anyway"',
             'Pain is the most common reason children present to the ED',
             'Untreated pain causes: physiologic stress, psychological trauma, prolonged ED stay',
             'GOAL: treat pain early, effectively, and multimodally — then reassess',
             '>Oligoanalgesia is not conservative. It is harm.',
         ],
         'notes': 'Start strong. The literature is clear that children are systematically undertreated for pain, particularly younger children, non-verbal children, and children of color. This is not acceptable. Frame the entire lecture around the principle that undertreating pain is an active failure, not a conservative approach.'},

        {'type': 'table', 'title': 'Age-Appropriate Pain Assessment Tools',
         'headers': ['Age Group', 'Tool', 'Scale', 'Key Points'],
         'rows': [
             ['Neonates', 'NIPS (Neonatal Infant Pain Scale)', '0-7', 'Facial expression, cry, breathing, arm/leg movement, alertness'],
             ['0-3 years', 'FLACC', '0-10', 'Face, Legs, Activity, Cry, Consolability — observational'],
             ['3-7 years', 'Wong-Baker FACES', '0-10', 'Point to the face that shows how you feel'],
             ['7+ years', 'Numeric Rating Scale (NRS)', '0-10', 'Self-report: "rate your pain 0 to 10"'],
             ['Non-verbal/cognitive impairment', 'r-FLACC (revised)', '0-10', 'Modified FLACC with individualized behaviors from caregivers'],
         ],
         'notes': 'Use the right tool for the right age. Self-report is the gold standard when available (NRS for older children). For pre-verbal children, FLACC is the standard observational tool. For children with cognitive impairment, use the revised FLACC with parent input on individualized pain behaviors. Document the score, treat, then reassess.'},

        {'type': 'algorithm', 'title': 'Multimodal Analgesia Framework',
         'steps': [
             'Step 1 — NON-PHARMACOLOGIC: Distraction, positioning, caregiver presence, child life, sucrose (neonates)',
             'Step 2 — TOPICAL/LOCAL: LET gel (lacerations), lidocaine cream (IV starts), buffered lidocaine injection',
             'Step 3 — ORAL NON-OPIOID: Ibuprofen 10 mg/kg + acetaminophen 15 mg/kg (best as combination)',
             'Step 4 — INTRANASAL: Fentanyl IN 1.5-2 mcg/kg (rapid onset, no IV needed)',
             'Step 5 — IV ANALGESIA: Ketorolac 0.5 mg/kg IV + morphine 0.1 mg/kg IV or ketamine 0.3 mg/kg IV',
             'Step 6 — REGIONAL ANESTHESIA: Nerve blocks (femoral, fascia iliaca, digital, etc.)',
             'Step 7 — PROCEDURAL SEDATION: For procedures requiring immobility and deeper analgesia',
         ],
         'notes': 'This framework should be internalized. The principle: layer your analgesia. Start with the foundation (non-pharmacologic + topical) and build up based on pain severity. The combination of ibuprofen + acetaminophen is as effective as ibuprofen + codeine for many painful conditions. Intranasal fentanyl is a game-changer — fast onset, no IV needed, bridge to definitive analgesia.'},

        {'type': 'table', 'title': 'Key Analgesic Dosing Reference',
         'headers': ['Drug', 'Route', 'Dose', 'Onset', 'Duration', 'Pearl'],
         'rows': [
             ['Ibuprofen', 'PO', '10 mg/kg (max 800)', '30 min', '6-8 h', 'Best oral analgesic for most ED pain'],
             ['Acetaminophen', 'PO/PR', '15 mg/kg (max 1g)', '30-60 min', '4-6 h', 'Combine with ibuprofen for synergy'],
             ['Ketorolac', 'IV/IM', '0.5 mg/kg (max 30)', '15-30 min', '6 h', 'IV NSAID; excellent for renal colic, MSK'],
             ['Fentanyl', 'IN', '1.5-2 mcg/kg (max 100)', '5-10 min', '30-60 min', 'Bridge analgesic; no IV needed'],
             ['Morphine', 'IV', '0.1 mg/kg (max 5)', '5-10 min', '3-4 h', 'Titrate q5-10min; watch for histamine release'],
             ['Ketamine', 'IV subdissociative', '0.3 mg/kg', '1-2 min', '15-20 min', 'Opioid-sparing; excellent for severe pain'],
             ['Lidocaine 1%', 'Local', 'Max 4.5 mg/kg', 'Immediate', '1-2 h', 'Buffer with bicarb 1:10 to reduce sting'],
             ['LET gel', 'Topical', 'Apply to wound x 20-30 min', '20-30 min', '1-2 h', 'Lidocaine-epi-tetracaine; non-facial lacs'],
         ],
         'notes': 'This is your dosing quick-reference. Key pearls: Ibuprofen + acetaminophen together is a powerhouse — give both. Intranasal fentanyl using the MAD device is one of the most useful skills in PEM. Sub-dissociative ketamine (0.3 mg/kg IV over 15 min) provides excellent analgesia without full dissociation.'},

        {'type': 'content', 'title': 'Regional Anesthesia in the PED',
         'bullets': [
             'Nerve blocks are increasingly used in PEM — ultrasound guidance has made them safer and more accessible',
             'Femoral nerve block: femur fractures — ultrasound-guided, landmark-based also effective',
             'Fascia iliaca compartment block (FICB): femur and hip fractures — easier to perform than femoral',
             'Digital nerve block: finger/toe lacerations, nailbed repairs, paronychia drainage',
             'Hematoma block: distal radius fractures — inject lidocaine directly into fracture hematoma',
             'Dental blocks: inferior alveolar, infraorbital — dental avulsions, lacerations',
             'Serratus anterior block: rib fractures, chest tube placement (emerging)',
             '>Blocks dramatically reduce opioid requirements and improve patient/parent satisfaction',
         ],
         'notes': 'Regional anesthesia is a growing field in PEM. The fascia iliaca block is the easiest to learn and most impactful — it provides excellent analgesia for femur fractures, which are among the most painful injuries in children. With ultrasound guidance, these blocks are safe and effective. If your fellowship program offers block training, prioritize it.'},

        {'type': 'two_column', 'title': 'Opioid-Sparing Strategies vs. When Opioids Are Needed',
         'left_title': 'OPIOID-SPARING APPROACH WORKS',
         'left': [
             'Mild-moderate pain (NRS 4-6)',
             'Ibuprofen + acetaminophen combination',
             'Fractures amenable to nerve block',
             'Lacerations with good local anesthesia',
             'Abdominal pain under evaluation',
             'Headache / migraine',
             'Post-procedural pain (most cases)',
         ],
         'right_title': 'OPIOIDS APPROPRIATE',
         'right': [
             'Severe pain (NRS 7-10) not controlled by non-opioids',
             'Sickle cell vaso-occlusive crisis',
             'Severe burns',
             'Multi-trauma',
             'Failed nerve block or nerve block not feasible',
             'Renal colic refractory to ketorolac',
             'Post-operative pain (per surgical team)',
         ],
         'notes': 'The point is NOT that opioids are bad. The point is that multimodal analgesia reduces the NEED for opioids. When opioids are indicated, give them. Do not let fear of opioids cause undertreating severe pain. A child with a femur fracture and NRS 9 needs opioids — and a nerve block — and ibuprofen. Layer everything.'},

        {'type': 'content', 'title': 'Non-Pharmacologic Pain Management',
         'bullets': [
             'Distraction: tablets, videos, bubbles, child life specialists — most effective non-pharmacologic tool',
             'Positioning: upright for abdominal pain, elevation for extremity injuries',
             'Caregiver presence and comfort: parents at bedside reduces distress significantly',
             'Sucrose (24%) for neonates: 0.5-1 mL on pacifier 2 min before procedure — effective through 3-6 months',
             'Comfort positioning: skin-to-skin (neonates), facilitated tucking, swaddling',
             'Cold therapy: ice packs for sprains, strains, and musculoskeletal injury',
             'Music therapy, virtual reality: emerging evidence for procedural pain in children',
             '>Non-pharmacologic methods are not a substitute for analgesia — they are an adjunct',
         ],
         'notes': 'Child life specialists are invaluable. If your department has them, use them for every painful procedure. Distraction has Level 1 evidence for reducing procedural pain and distress. Sucrose for neonates is one of the most well-studied interventions in PEM — it works, it is safe, and it is easy. But these are adjuncts — do not use distraction INSTEAD of analgesia.'},

        {'type': 'case', 'title': '6-Year-Old with Displaced Forearm Fracture',
         'scenario': 'A 6-year-old boy (22 kg) arrives by EMS with a displaced distal radius fracture after falling off monkey bars. He is crying, guarding the arm, and rates his pain 9/10. The arm is angulated and swollen. Parents are anxious. IV access has not yet been obtained.',
         'questions': [
             'What is your FIRST analgesic intervention? (Intranasal fentanyl 1.5 mcg/kg = 33 mcg via MAD device)',
             'What do you give while waiting for ortho? (Ibuprofen 220 mg PO + acetaminophen 330 mg PO)',
             'IV is placed — next? (Hematoma block with lidocaine into fracture site OR ketamine 0.3 mg/kg for analgesia)',
             'Reduction needed — sedation plan? (Ketamine IV 1.5 mg/kg + ondansetron + pre-med with midazolam)',
         ],
         'notes': 'This is a perfect case for demonstrating layered multimodal analgesia. The child arrives in severe pain with no IV — intranasal fentanyl is the bridge. Then add oral ibuprofen + acetaminophen as the foundation. Then consider hematoma block for focal analgesia. Finally, procedural sedation for the reduction. Each layer builds on the previous.'},

        {'type': 'pitfalls', 'title': 'Pediatric Analgesia Pitfalls',
         'items': [
             'Waiting for IV access before treating pain — use IN fentanyl, oral meds, or topicals immediately',
             'Not reassessing pain after interventions — treat, score, re-treat if needed',
             'Using codeine in children — FDA boxed warning due to ultra-rapid metabolizer deaths',
             'Forgetting to buffer lidocaine — add 1 mL NaHCO3 per 9 mL lidocaine to dramatically reduce injection pain',
             'Withholding analgesia for abdominal pain pending surgical evaluation — evidence shows this does NOT mask surgical findings',
             'Over-relying on oral opioids (oxycodone/hydrocodone) when non-opioids would suffice',
             'Not using topical anesthesia (LET gel) for lacerations — it should be applied at triage',
         ],
         'notes': 'The codeine warning is critical: CYP2D6 ultra-rapid metabolizers convert codeine to morphine at dangerous levels. Multiple pediatric deaths have occurred. Codeine should NOT be used in children. Tramadol carries similar concerns. The abdominal pain myth is the most harmful — multiple RCTs show analgesia does not impair surgical evaluation.'},
    ],
    'takeaways': [
        'Treat pain early and reassess — oligoanalgesia in children is harm, not conservatism',
        'Intranasal fentanyl (1.5 mcg/kg via MAD) is the best bridge analgesic when there is no IV',
        'Ibuprofen + acetaminophen together is as effective as many opioid combinations',
        'Regional anesthesia (fascia iliaca, hematoma block) dramatically reduces opioid need',
        'Never use codeine in children — FDA black box warning for ultra-rapid metabolizer deaths',
    ],
    'references': [
        'Fein JA, et al. Relief of pain and anxiety in pediatric patients in emergency medical systems. Pediatrics. 2012;130(5):e1391-1405.',
        'Poonai N, et al. Oral administration of morphine versus ibuprofen to manage postfracture pain in children: a randomized trial. CMAJ. 2014;186(18):1358-1363.',
        'Kendall JM, et al. Multicentre randomised controlled trial of nasal diamorphine for analgesia in children presenting to emergency departments with clinical fractures. BMJ. 2001;322(7281):261-265.',
        'Bhatt M, et al. Association between fasting status and adverse events in pediatric procedural sedation. JAMA Pediatr. 2018;172(7):678-685.',
        'American Academy of Pediatrics. Codeine: time to say "no." Pediatrics. 2016;138(4):e20162396.',
        'Suresh S, et al. Regional anesthesia for pediatric patients — an update. Curr Opin Anaesthesiol. 2014;27(5):556-563.',
    ],
}

if __name__ == '__main__':
    build_deck(spec, os.path.join(os.path.dirname(__file__), '11_pediatric_analgesia.pptx'))

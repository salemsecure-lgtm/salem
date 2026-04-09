#!/usr/bin/env python3
"""Build Deck 14: Head Trauma and Pediatric TBI"""
import sys, os
sys.path.insert(0, os.path.dirname(__file__))
from build_deck import build_deck

spec = {
    'number': 14,
    'short_title': 'Pediatric Head Trauma',
    'title': 'Head Trauma and Pediatric TBI',
    'subtitle': 'PECARN Decision Rules, Acute TBI Management, and Neuroprotective Strategies',
    'objectives': [
        'Apply the PECARN head injury prediction rule to safely reduce unnecessary CT scans',
        'Classify TBI severity and understand GCS interpretation in children',
        'Execute the initial management of moderate-severe TBI: ABCs, ICP management, neuroprotection',
        'Identify children who need neurosurgical intervention vs. observation',
        'Recognize non-accidental head trauma patterns in infants',
    ],
    'slides': [
        {'type': 'content', 'title': 'Pediatric Head Trauma in the ED',
         'bullets': [
             'Head trauma is the #1 cause of traumatic death and disability in children',
             '~500,000 pediatric ED visits/year for head trauma in the US',
             'Most (>95%) are minor — the challenge is identifying the <5% with clinically important TBI (ciTBI)',
             'ciTBI = death, neurosurgery, intubation >24h, or hospitalization ≥2 nights',
             'CT use has increased dramatically — but most scans are normal (>90%)',
             'PECARN prediction rule: evidence-based tool to safely reduce unnecessary CT radiation',
             '>The goal: identify every child who needs a CT while protecting the vast majority who do not',
         ],
         'notes': 'Frame this talk around the core PEM challenge: pediatric head trauma is incredibly common, but clinically important injuries are rare. The risk of missing a neurosurgical lesion must be balanced against the risk of ionizing radiation (estimated 1 in 5,000-10,000 lifetime cancer risk per CT). PECARN provides the evidence-based framework.'},

        {'type': 'algorithm', 'title': 'PECARN Prediction Rule: Age <2 Years',
         'steps': [
             'GCS ≤14, palpable skull fracture, or altered mental status? → YES → CT recommended (ciTBI risk 4.4%)',
             'Occipital/parietal/temporal scalp hematoma, LOC ≥5s, severe mechanism, not acting normally per parent? → YES → CT vs. observation based on clinical factors (ciTBI risk 0.9%)',
             'None of the above? → CT NOT recommended — observation adequate (ciTBI risk <0.02%)',
         ],
         'notes': 'This is the PECARN rule for children under 2. The decision to observe vs. CT for the intermediate-risk group is based on physician experience, worsening symptoms, age <3 months, and parental preference. Key: "not acting normally" per parents is a PECARN criterion — parents know their child. Occipital/parietal/temporal hematomas are higher risk than frontal hematomas in this age group.'},

        {'type': 'algorithm', 'title': 'PECARN Prediction Rule: Age ≥2 Years',
         'steps': [
             'GCS ≤14 or signs of basilar skull fracture or altered mental status? → YES → CT recommended (ciTBI risk 4.3%)',
             'LOC, vomiting, severe mechanism, or severe headache? → YES → CT vs. observation based on clinical factors (ciTBI risk 0.8%)',
             'None of the above? → CT NOT recommended — observation adequate (ciTBI risk <0.05%)',
         ],
         'notes': 'For children ≥2 years, the rule is similar but uses different variables. Signs of basilar skull fracture: raccoon eyes, Battle sign, hemotympanum, CSF otorrhea/rhinorrhea. Severe mechanism: MVC with ejection/rollover/death of another, pedestrian/cyclist hit by vehicle, fall >5 feet (or >3 feet if <2y), head struck by high-impact object. The negative predictive value of PECARN is 99.95%.'},

        {'type': 'table', 'title': 'GCS in Children: Age-Adapted Assessment',
         'headers': ['Component', 'Adult Response', 'Pediatric (<2y) Adaptation', 'Score'],
         'rows': [
             ['Eye Opening', 'Spontaneous', 'Spontaneous', '4'],
             ['', 'To voice', 'To voice', '3'],
             ['', 'To pain', 'To pain', '2'],
             ['', 'None', 'None', '1'],
             ['Verbal', 'Oriented', 'Coos/babbles', '5'],
             ['', 'Confused', 'Irritable cry', '4'],
             ['', 'Inappropriate', 'Cries to pain', '3'],
             ['', 'Incomprehensible', 'Moans to pain', '2'],
             ['', 'None', 'None', '1'],
             ['Motor', 'Obeys commands', 'Spontaneous movement', '6'],
             ['', 'Localizes pain', 'Withdraws to touch', '5'],
             ['', 'Flexion withdrawal', 'Withdraws to pain', '4'],
             ['', 'Abnormal flexion', 'Abnormal flexion', '3'],
             ['', 'Extension', 'Extension', '2'],
             ['', 'None', 'None', '1'],
         ],
         'notes': 'GCS must be adapted for pre-verbal children. The verbal scale is most affected — an alert, babbling 6-month-old gets a V5, not a V1. Mild TBI = GCS 13-15, moderate = 9-12, severe = 3-8. GCS ≤8 = intubation indicated for airway protection. Serial GCS monitoring is essential — a drop of ≥2 points is concerning for worsening TBI.'},

        {'type': 'content', 'title': 'Moderate to Severe TBI: Initial ED Management',
         'bullets': [
             'ABCs with C-spine immobilization until cleared',
             'Airway: GCS ≤8 → RSI intubation (avoid hypotension, avoid hypoxia)',
             'RSI agents: etomidate 0.3 mg/kg or ketamine 1-2 mg/kg + rocuronium 1 mg/kg',
             'Avoid succinylcholine (theoretical ICP elevation) — use rocuronium',
             'Target: SpO2 >90%, PaCO2 35-40 mmHg (avoid hyperventilation unless herniation)',
             'Circulation: maintain SBP >70 + (2 × age) mmHg — hypotension doubles mortality',
             'ICP management: HOB 30°, midline head, prevent hyperthermia, analgesia/sedation',
             'Hyperosmolar therapy for herniation: 3% NaCl 3-5 mL/kg IV bolus or mannitol 0.5-1 g/kg',
         ],
         'notes': 'The ABCs of TBI management are modified: avoid the "lethal triad" of hypoxia, hypotension, and hyperventilation. Each of these independently worsens outcomes. Ketamine is now accepted for RSI in TBI — the old concern about ICP elevation is not supported by evidence. Prophylactic hyperventilation is harmful — only hyperventilate (target pCO2 30-35) if there are signs of active herniation.'},

        {'type': 'key_point', 'title': 'Critical Concept',
         'point': 'Hypotension in TBI doubles mortality. Prevent it aggressively.',
         'sub': 'Even a single episode of hypotension (SBP <70 + 2×age) in a child with TBI significantly worsens outcomes. Resuscitate with isotonic crystalloid or blood products. Do NOT restrict fluids in TBI — that is outdated dogma.',
         'notes': 'This is one of the most impactful teaching points. Data from the TBI-ADAPT and other studies show that hypotension in pediatric TBI is the single most modifiable risk factor for poor outcomes. Aggressive fluid resuscitation (while avoiding fluid overload) is essential. The old practice of "keeping them dry" to reduce brain edema is harmful.'},

        {'type': 'two_column', 'title': 'When to Get Neurosurgery Involved',
         'left_title': 'NEUROSURGERY CONSULT',
         'left': [
             'GCS ≤12 or declining GCS',
             'Epidural hematoma (any size)',
             'Subdural hematoma with midline shift',
             'Depressed skull fracture',
             'Open/compound skull fracture',
             'Penetrating head injury',
             'Growing skull fracture (leptomeningeal cyst)',
             'Signs of herniation (blown pupil, posturing, Cushing triad)',
         ],
         'right_title': 'OBSERVATION APPROPRIATE',
         'right': [
             'GCS 14-15, normal neurologic exam',
             'Small, non-displaced linear skull fracture',
             'Small subdural or subarachnoid hemorrhage without midline shift',
             'Improving mental status on serial exams',
             'No surgical lesion on CT',
             'Concussion (mild TBI) with normal CT or observation per PECARN',
         ],
         'notes': 'The distinction is between surgical and non-surgical lesions. Any epidural hematoma needs neurosurgery involvement because they can expand rapidly (middle meningeal artery). Subdural hematomas are more commonly venous and may be observed if small without shift, but neurosurgery should still be aware. Any herniation signs = emergent neurosurgery.'},

        {'type': 'content', 'title': 'Non-Accidental Head Trauma (Abusive Head Trauma)',
         'bullets': [
             '#1 cause of traumatic death in infants — must ALWAYS be on your differential',
             'Suspect when: no witnessed mechanism, mechanism inconsistent with injuries, delay in seeking care',
             'Classic triad: subdural hematomas, retinal hemorrhages, encephalopathy',
             'Additional findings: multiple or complex skull fractures, differing ages of injuries, other injuries (rib fractures, metaphyseal fractures)',
             'Workup: CT head, skeletal survey, ophthalmology exam, consider MRI, coagulation studies, liver/pancreatic enzymes',
             'Mandatory reporting: you are legally required to report suspected child abuse',
             '>If the history does not match the injuries, it is your duty to pursue the real cause',
         ],
         'notes': 'This slide requires sensitivity and directness. Abusive head trauma kills more infants than any other form of trauma. The mechanism (usually violent shaking ± impact) produces a characteristic pattern. Fellows must be comfortable recognizing this pattern and initiating the workup and reporting process. This is not optional — it is legally mandated.'},

        {'type': 'case', 'title': '3-Year-Old Fall from Slide',
         'scenario': 'A 3-year-old boy fell from a 6-foot slide at the playground. He cried immediately, then vomited once. No LOC per the babysitter who witnessed the fall. On exam: GCS 15, alert and interactive, large frontal scalp hematoma, no other injuries. Neurologic exam is normal.',
         'questions': [
             'Apply PECARN (≥2 years): GCS 14? No. Basilar skull fracture signs? No. AMS? No.',
             'LOC? No. Vomiting? Yes (1 episode). Severe mechanism? Yes (>5 ft fall). Severe headache? No.',
             'PECARN intermediate risk: CT vs. observation → consider observation with serial neuro checks',
             'Decision factors: single vomit, frontal hematoma (lower risk), well-appearing, close follow-up available',
             'Plan: observe 4-6 hours, serial neuro checks, discharge if improving with strict return precautions',
         ],
         'notes': 'This case demonstrates real-time PECARN application. The child has one intermediate-risk factor (vomiting) and arguably a second (fall >5 ft). PECARN suggests either CT or observation. Clinical judgment: the child is GCS 15, interactive, has a frontal (not parietal/occipital) hematoma, and the single vomit after head injury is common. Observation with serial exams is reasonable.'},

        {'type': 'pitfalls', 'title': 'Pediatric Head Trauma Pitfalls',
         'items': [
             'CT scanning every child with a head bump — PECARN helps reduce unnecessary radiation',
             'Prophylactic hyperventilation — harmful in TBI; only hyperventilate for active herniation',
             'Hypotension in TBI — even one episode doubles mortality; resuscitate aggressively',
             'Missing non-accidental trauma — always consider NAT in infants with head injuries and unclear mechanism',
             'Not doing serial neurologic exams — GCS can drop; reassess frequently',
             'Discharging after a single normal neurologic exam — observe at least 4-6 hours for intermediate-risk patients',
             'Using GCS in isolation — a GCS of 14 means different things at 3 months vs. 3 years; use age-adapted version',
         ],
         'notes': 'The two biggest pitfalls: overtesting (CT-ing everyone) and undertesting (missing the child with a surgical lesion). PECARN is the bridge. The other critical pitfall is missing NAT — this requires active consideration, not passive discovery.'},
    ],
    'takeaways': [
        'PECARN identifies children at very low risk for ciTBI — use it to safely avoid unnecessary CT scans',
        'Moderate-severe TBI: prevent the lethal triad of hypoxia, hypotension, and hyperventilation',
        'Hypotension in TBI doubles mortality — resuscitate aggressively with isotonic fluid',
        'Any epidural hematoma needs neurosurgical consultation; signs of herniation = emergent hypertonic saline',
        'Always consider non-accidental trauma in infants with head injuries and unclear/inconsistent mechanism',
    ],
    'references': [
        'Kuppermann N, et al. Identification of children at very low risk of clinically-important brain injuries after head trauma: a prospective cohort study (PECARN). Lancet. 2009;374(9696):1160-1170.',
        'Kochanek PM, et al. Guidelines for the management of pediatric severe TBI, 3rd edition. Pediatr Crit Care Med. 2019;20(1S):S1-S82.',
        'Adelson PD, et al. Guidelines for the acute medical management of severe TBI in infants, children, and adolescents. Pediatr Crit Care Med. 2003;4(3 Suppl):S1-75.',
        'Christian CW, et al. Abusive head trauma in infants and children. Pediatrics. 2009;123(5):1409-1411.',
        'Babl FE, et al. Accuracy of PECARN, CATCH, and CHALICE head injury decision rules in children: a prospective cohort study. Lancet. 2017;389(10087):2393-2402.',
    ],
}

if __name__ == '__main__':
    build_deck(spec, os.path.join(os.path.dirname(__file__), '14_head_trauma_tbi.pptx'))

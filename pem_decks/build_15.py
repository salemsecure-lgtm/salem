#!/usr/bin/env python3
"""Build Deck 15: Multisystem Trauma in Children"""
import sys, os
sys.path.insert(0, os.path.dirname(__file__))
from build_deck import build_deck

spec = {
    'number': 15,
    'short_title': 'Pediatric Trauma',
    'title': 'Multisystem Trauma in Children',
    'subtitle': 'Primary Survey, Resuscitation, and Pediatric-Specific Trauma Patterns',
    'objectives': [
        'Execute the pediatric primary and secondary survey systematically',
        'Understand how pediatric anatomy affects injury patterns in trauma',
        'Manage hemorrhagic shock in the pediatric trauma patient',
        'Apply evidence-based decision rules for imaging in pediatric trauma',
        'Identify injuries unique to or more common in children',
    ],
    'slides': [
        {'type': 'content', 'title': 'Pediatric Trauma: Why Children Are Different',
         'bullets': [
             'Trauma is the #1 cause of death in children >1 year — #1 cause of years of life lost',
             'Smaller body mass → force distributed over larger relative area → multiorgan injury more common',
             'Proportionally larger head → head injury in almost all pediatric multisystem trauma',
             'More compliant chest wall → pulmonary contusion without rib fractures (rib fx in kids = massive force)',
             'Solid organs proportionally larger and less protected → spleen and liver injury more common',
             'Higher body surface area-to-mass ratio → hypothermia develops rapidly',
             '>Rib fractures in children suggest MASSIVE force — have a high index of suspicion for associated injuries',
         ],
         'notes': 'These anatomic differences drive every pediatric-specific modification in trauma management. The key concept: children distribute force differently. A child hit by a car sustains head, chest, and abdominal injuries simultaneously — multisystem injury is the rule, not the exception.'},

        {'type': 'algorithm', 'title': 'Pediatric Primary Survey: ABCDE',
         'steps': [
             'A — Airway with C-spine: jaw thrust, suction, age-appropriate equipment, inline stabilization',
             'B — Breathing: assess bilateral breath sounds, chest wall movement, SpO2; needle decompression if tension',
             'C — Circulation: pulse quality, capillary refill, 2 large-bore IVs or IO; 20 mL/kg NS then blood',
             'D — Disability: GCS (age-adapted), pupils, glucose, brief neuro exam',
             'E — Exposure/Environment: fully undress, log-roll, prevent hypothermia aggressively',
         ],
         'notes': 'The primary survey is identical in structure to adults but every step has pediatric modifications. Airway: use appropriately sized equipment (blade, ETT = [age/4 + 3.5] cuffed). Breathing: do NOT assume clear lungs mean no injury — children can have massive pulmonary contusion without rib fractures. Circulation: IO access is preferred over central line in emergencies. Hypothermia prevention is critical.'},

        {'type': 'table', 'title': 'Pediatric Vital Signs by Age (Trauma Context)',
         'headers': ['Age', 'Heart Rate', 'Systolic BP', 'Blood Volume', 'Estimated Weight'],
         'rows': [
             ['Newborn', '120-160', '60-70', '80 mL/kg', '3.5 kg'],
             ['6 months', '120-140', '70-80', '80 mL/kg', '7 kg'],
             ['1 year', '110-130', '80-90', '80 mL/kg', '10 kg'],
             ['3 years', '100-120', '85-95', '80 mL/kg', '15 kg'],
             ['6 years', '90-110', '90-100', '80 mL/kg', '20 kg'],
             ['10 years', '80-100', '95-110', '80 mL/kg', '30 kg'],
             ['14 years', '70-90', '100-120', '70 mL/kg', '50 kg'],
         ],
         'notes': 'Tachycardia is the EARLIEST sign of hemorrhagic shock in children — do not wait for hypotension. Children maintain blood pressure through vasoconstriction until they have lost ~30% of blood volume, then decompensate rapidly. By the time a child is hypotensive, they are in severe shock. Weight estimation: (age × 2) + 10 for children 1-10 years; use Broselow tape in the ED.'},

        {'type': 'content', 'title': 'Hemorrhagic Shock: Pediatric Resuscitation',
         'bullets': [
             'Estimated blood volume: 80 mL/kg (70 mL/kg in adolescents)',
             'Classify hemorrhagic shock: Class I (<15%), II (15-30%), III (30-40%), IV (>40%)',
             'Class I-II: tachycardia only, normal BP — easy to miss in children',
             'Class III-IV: hypotension, altered mental status, mottled skin — decompensated shock',
             'Resuscitation: 20 mL/kg isotonic crystalloid → if no response, 10-20 mL/kg pRBCs → massive transfusion protocol',
             'Massive transfusion: 1:1:1 ratio of pRBC:FFP:platelets (10 mL/kg each)',
             'TXA: tranexamic acid 15-20 mg/kg (max 1g) IV over 10 min within 3 hours of injury',
             '>If a child needs >40 mL/kg total fluid in the first hour, activate massive transfusion protocol',
         ],
         'notes': 'Key teaching: tachycardia is the first sign, not hypotension. A child with HR 160 and normal BP after trauma may have already lost 25% of blood volume. Resuscitate early and aggressively. TXA within 3 hours of injury reduces mortality in adult trauma (CRASH-2) and is increasingly used in pediatric trauma.'},

        {'type': 'two_column', 'title': 'Common Pediatric Injury Patterns',
         'left_title': 'BLUNT TRAUMA (Most Common)',
         'left': [
             'MVC: #1 cause of pediatric trauma death',
             'Pedestrian struck: Waddell triad — femur fx + chest/abdominal injury + head injury',
             'Falls: head injury predominant in young children due to large head',
             'Sports: concussion, c-spine, solid organ injury',
             'Bicycle: handlebar injuries → duodenal/pancreatic injury',
             'Lap belt complex: lumbar spine (Chance fx) + hollow viscus injury',
         ],
         'right_title': 'PENETRATING TRAUMA',
         'right': [
             'Less common in children but increasing',
             'Gunshot wounds: assess trajectory, damage path',
             'Stab wounds: depth and trajectory determine management',
             'Impalement: do NOT remove in field — remove in OR',
             'High-velocity projectile: massive tissue damage beyond wound track',
             'Always assess for concomitant injuries',
         ],
         'notes': 'Blunt trauma accounts for >85% of pediatric trauma. The Waddell triad (pedestrian struck) is classic boards material. Handlebar injuries are important to recognize — a child who falls onto bicycle handlebars can sustain duodenal hematoma or pancreatic injury. Lap belt injuries (Chance fracture + bowel injury) occur because the lap belt rides up on a small child.'},

        {'type': 'content', 'title': 'Abdominal Trauma: Solid Organ Injury',
         'bullets': [
             'Spleen and liver are the most commonly injured solid organs in pediatric blunt trauma',
             'Most solid organ injuries in children are managed NON-OPERATIVELY (>90% for spleen)',
             'FAST exam: useful to identify free fluid, but operator-dependent and can miss solid organ injury',
             'CT with IV contrast: gold standard for solid organ injury grading',
             'Indications for operative management: hemodynamic instability despite resuscitation, peritonitis, high-grade injury with ongoing bleeding',
             'Hollow viscus injury: less common but important — seatbelt sign, free fluid without solid organ injury on CT',
             '>Hemodynamically stable child with solid organ injury → non-operative management is the standard',
         ],
         'notes': 'The paradigm shift in pediatric trauma surgery: non-operative management of solid organ injuries is the standard. This was pioneered at Toronto Hospital for Sick Children and is now universal. The PEM role: identify the injury, ensure hemodynamic stability, and admit for observation. Operative intervention is reserved for hemodynamic instability despite adequate resuscitation.'},

        {'type': 'content', 'title': 'C-Spine Clearance in Pediatric Trauma',
         'bullets': [
             'C-spine injuries are less common in children but carry high morbidity',
             'Children <8 years: injuries tend to be at C1-C3 (large head, weak ligaments)',
             'Children >8 years: injury patterns similar to adults (lower C-spine)',
             'SCIWORA: Spinal Cord Injury Without Radiographic Abnormality — more common in children',
             'Clearance in alert, non-intoxicated child: no midline tenderness, no focal deficit, no painful distracting injury → clinically clear',
             'If imaging needed: CT is standard (X-ray has poor sensitivity in children)',
             'If concern for SCIWORA despite normal CT: obtain MRI within 72 hours',
             '>Use appropriate-sized collars. A poorly fitting collar is worse than no collar — it obscures the exam and can compromise the airway.',
         ],
         'notes': 'SCIWORA is a uniquely pediatric entity — the pediatric spine is more elastic than the spinal cord, so the cord can be injured even though the bones and ligaments look normal on imaging. If a child has neurologic deficits but normal CT, SCIWORA is the diagnosis and MRI is mandatory. Clinical clearance in alert, cooperative children is safe and reduces unnecessary imaging.'},

        {'type': 'case', 'title': '6-Year-Old Pedestrian Struck by Car',
         'scenario': 'A 6-year-old boy (22 kg) is brought by EMS after being struck by a car at ~25 mph. He was thrown 10 feet. On arrival: crying, confused, right leg deformed, abdominal tenderness. HR 150, BP 85/60, RR 32, SpO2 96% on 15L NRB. GCS 13 (E3V4M6). Large frontal scalp laceration actively bleeding.',
         'questions': [
             'Primary survey priorities? (Airway clear, c-spine immobilized, breathing adequate, circulation = shock)',
             'Is this child in shock? (Yes — tachycardia + borderline BP + mechanism → Class II-III hemorrhage)',
             'Resuscitation? (20 mL/kg NS bolus × 2, type and crossmatch, prepare for blood)',
             'Imaging? (FAST exam immediately, CT head + C-spine + chest + abdomen/pelvis with IV contrast)',
             'This is a Waddell triad: head injury + leg fracture + abdominal injury → systematic evaluation of all three',
         ],
         'notes': 'Classic PEM trauma case. Walk through the primary survey step by step. The child has multiple system involvement (head, abdomen, extremity) — classic for pedestrian struck. The HR of 150 with BP 85/60 is compensated shock. Do not wait for frank hypotension. The FAST exam helps triage — if positive for free fluid with instability, go to OR. If stable, CT for definitive evaluation.'},

        {'type': 'pitfalls', 'title': 'Pediatric Trauma Pitfalls',
         'items': [
             'Waiting for hypotension to diagnose shock — tachycardia is the FIRST sign; hypotension is LATE',
             'Not preventing hypothermia — children lose heat rapidly; use warming blankets, warm fluids, warm room',
             'Treating rib fractures as minor — in children, rib fractures indicate MASSIVE force',
             'Missing handlebar injuries — duodenal hematoma and pancreatic injury have delayed presentation',
             'Not considering non-accidental trauma — especially in infants and toddlers with unexplained injuries',
             'Over-relying on FAST — sensitivity for solid organ injury is limited; use CT when indicated',
             'Failure to activate massive transfusion protocol early — if >40 mL/kg resuscitation in first hour, activate MTP',
         ],
         'notes': 'Each of these pitfalls has led to preventable morbidity or mortality in pediatric trauma. Hypothermia is the "silent killer" — it worsens coagulopathy, acidosis, and outcomes. The rib fracture pearl is critical: child ribs are flexible, so fractures require enormous force and should trigger concern for underlying injuries (pulmonary contusion, liver/spleen laceration, cardiac injury).'},
    ],
    'takeaways': [
        'Tachycardia is the FIRST sign of hemorrhagic shock in children — do not wait for hypotension',
        'Prevent the trauma triad of death: hypothermia, acidosis, coagulopathy',
        'Most solid organ injuries (spleen, liver) in children are managed non-operatively if hemodynamically stable',
        'Rib fractures in children = massive force — always look for associated injuries',
        'TXA (15-20 mg/kg IV) within 3 hours of injury for significant hemorrhage',
    ],
    'references': [
        'ATLS Subcommittee. Advanced Trauma Life Support. 10th ed. American College of Surgeons; 2018.',
        'Stylianos S. Evidence-based guidelines for resource utilization in children with isolated spleen or liver injury. J Pediatr Surg. 2000;35(2):164-169.',
        'Holmes JF, et al. Identifying children at very low risk of clinically important blunt abdominal injuries. Ann Emerg Med. 2013;62(2):107-116.',
        'CRASH-2 Trial Collaborators. Effects of tranexamic acid on death, vascular occlusive events, and blood transfusion in trauma patients. Lancet. 2010;376(9734):23-32.',
        'Leonard JC, et al. PECARN cervical spine injury study. Ann Emerg Med. 2019;74(2):135-148.',
        'Pang D, Wilberger JE. Spinal cord injury without radiographic abnormalities in children. J Neurosurg. 1982;57(1):114-129.',
    ],
}

if __name__ == '__main__':
    build_deck(spec, os.path.join(os.path.dirname(__file__), '15_multisystem_trauma.pptx'))

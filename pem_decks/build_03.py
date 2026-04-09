#!/usr/bin/env python3
"""Build Deck 3: Pediatric Shock — Recognition and Initial Management"""
import sys, os
sys.path.insert(0, os.path.dirname(__file__))
from build_deck import build_deck
from pem_design_system import TEAL, CORAL, NAVY, AMBER

spec = {
    'number': 3,
    'short_title': 'Pediatric Shock',
    'title': 'Pediatric Shock:\nRecognition and Initial Management',
    'subtitle': 'Classification, Early Recognition, Fluid Resuscitation, Vasopressors, and Escalation',
    'objectives': [
        'Classify shock by etiology: hypovolemic, distributive, cardiogenic, obstructive',
        'Recognize compensated shock before hemodynamic collapse',
        'Execute evidence-based fluid resuscitation and know when to stop',
        'Initiate vasopressor therapy for fluid-refractory shock',
        'Identify cardiogenic and obstructive shock requiring different management',
    ],
    'slides': [
        {'type': 'content', 'title': 'Why Shock Is Different in Children',
         'bullets': [
             'Children have HIGHER sympathetic reserve than adults — they compensate beautifully',
             'Cardiac output is heart rate–dependent (limited stroke volume reserve)',
             'Blood pressure is maintained until 25-30% volume is lost — then rapid collapse',
             'Tachycardia is the earliest and most sensitive sign of shock in children',
             'Hypotension = decompensated shock = near-arrest → act immediately',
             '>The goal: recognize and treat COMPENSATED shock before decompensation',
         ],
         'notes': 'Set the stage with this critical concept. Adults drop their blood pressure early; children maintain it until catastrophic decompensation. This means relying on BP to diagnose shock in children will miss the treatable window. Teach your team to recognize tachycardia, poor perfusion, and altered mental status as shock until proven otherwise.'},

        {'type': 'table', 'title': 'Classification of Shock in Children',
         'headers': ['Type', 'Mechanism', 'Common Causes', 'Key Features'],
         'rows': [
             ['Hypovolemic', 'Volume loss', 'Dehydration, hemorrhage, burns', 'Tachycardia, poor turgor, dry mucosa'],
             ['Distributive', 'Vasodilation', 'Sepsis, anaphylaxis, neurogenic', 'Warm/bounding (early), flash cap refill'],
             ['Cardiogenic', 'Pump failure', 'Myocarditis, CHD, arrhythmia', 'Hepatomegaly, JVD, gallop, pulm edema'],
             ['Obstructive', 'Flow obstruction', 'Tension pneumo, tamponade, PE', 'Distended neck veins, muffled hearts'],
         ],
         'notes': 'Hypovolemic is the most common type in children worldwide. Distributive (mostly sepsis) is the most common cause of shock requiring ICU admission. The critical distinction is that cardiogenic and obstructive shock are WORSENED by aggressive fluid resuscitation — you must identify the type before reflexively pushing fluids.'},

        {'type': 'two_column', 'title': 'Warm Shock vs Cold Shock',
         'left_title': 'WARM SHOCK (Early Distributive)',
         'left': [
             'Bounding pulses, wide pulse pressure',
             'Flash capillary refill (<1 second)',
             'Warm, flushed extremities',
             'Tachycardia',
             'Normal or elevated cardiac output',
             'Low SVR',
             'First-line vasopressor: NOREPINEPHRINE',
             'Think: early sepsis, anaphylaxis',
         ],
         'right_title': 'COLD SHOCK (Late/Hypovolemic)',
         'right': [
             'Weak, thready pulses',
             'Prolonged capillary refill (>3 sec)',
             'Cool, mottled, pale extremities',
             'Tachycardia → bradycardia (late)',
             'Low cardiac output',
             'High SVR (compensatory)',
             'First-line vasopressor: EPINEPHRINE',
             'Think: late sepsis, hypovolemia, cardiogenic',
         ],
         'notes': 'This distinction guides vasopressor selection. Warm shock has high output but low resistance — these patients need a vasopressor with alpha activity (norepinephrine). Cold shock has low output — they need an inotrope/chronotrope (epinephrine). Most children with septic shock present with COLD shock, unlike adults.'},

        {'type': 'content', 'title': 'Clinical Recognition of Shock — Beyond Vital Signs',
         'bullets': [
             'MENTAL STATUS: irritability → lethargy → unresponsiveness (tracks severity)',
             'SKIN: color (pallor, mottling, cyanosis), temperature, capillary refill',
             '>Capillary refill >2 seconds in a warm environment = abnormal',
             'PULSES: quality (bounding vs thready), central vs peripheral differential',
             'URINE OUTPUT: <1 mL/kg/hr (infant), <0.5 mL/kg/hr (child) = end-organ hypoperfusion',
             'HEART RATE: tachycardia out of proportion to fever/pain/crying = shock',
             'LACTATE: >2 mmol/L suggests tissue hypoperfusion; >4 = severe shock',
         ],
         'notes': 'Mental status is the most important clinical indicator of perfusion. A child with tachycardia who is interactive and consolable is different from one who is limp and unresponsive with the same heart rate. Capillary refill is imperfect but useful in a warm environment. Lactate is increasingly used as a bedside marker — point-of-care testing makes this rapidly available.'},

        {'type': 'algorithm', 'title': 'Fluid Resuscitation Pathway',
         'steps': [
             'Establish IV/IO access — 2 attempts PIV then IO immediately',
             '20 mL/kg isotonic crystalloid (NS or LR) push over 5-20 min',
             'Reassess: HR, perfusion, mental status, cap refill, lactate',
             'No improvement → repeat 20 mL/kg bolus (up to 40-60 mL/kg in 1st hour)',
             'Fluid-refractory → START VASOPRESSORS (do not give >60 mL/kg without pressors)',
             'Reassess volume status — consider echo for cardiac function and IVC',
             'If cardiogenic suspected: STOP FLUIDS, start inotrope (milrinone/epinephrine)',
         ],
         'notes': 'The 20-20-20 approach is the standard: give 20 mL/kg, reassess, repeat. After 40-60 mL/kg without improvement, you are dealing with fluid-refractory shock and need vasopressors. The FEAST trial raised caution about aggressive fluid boluses in resource-limited settings, but in well-monitored ED settings, early aggressive fluids remain standard for septic and hypovolemic shock. The exception is cardiogenic shock — fluid overload kills.'},

        {'type': 'content', 'title': 'Vascular Access: The IO Is Your Friend',
         'bullets': [
             'Peripheral IV: 2 attempts, 90 seconds max — then go IO',
             'IO sites: proximal tibia (1 cm below tuberosity, medial flat surface)',
             '>Also: distal tibia (above medial malleolus), distal femur, humeral head',
             'IO works for ALL resuscitation drugs, fluids, and blood products',
             'Flow rates: use pressure bag for rapid boluses through IO',
             'Complications: compartment syndrome (rare), osteomyelitis (<1%), extravasation',
             'Contraindications: fracture in same bone, prior IO same bone <48 hr, overlying infection',
             '>In arrest: IO is the FIRST-LINE access if no existing IV',
         ],
         'notes': 'The IO is the single most important procedural skill for shock management. Studies show it can be placed in 30-60 seconds with modern devices. The proximal tibia is the standard site for all ages. Flow rates are adequate for resuscitation — use a pressure bag for boluses. Every PEM fellow should be able to place an IO in their sleep.'},

        {'type': 'table', 'title': 'Vasopressor and Inotrope Selection',
         'headers': ['Agent', 'Dose Range', 'Primary Effect', 'Indication', 'Key Concern'],
         'rows': [
             ['Epinephrine', '0.05-0.3 mcg/kg/min', 'Inotropy + vasoconstriction', 'Cold shock, post-arrest', 'Tachyarrhythmia, lactate elevation'],
             ['Norepinephrine', '0.05-2 mcg/kg/min', 'Vasoconstriction + inotropy', 'Warm shock (sepsis)', 'Tissue ischemia at high doses'],
             ['Dopamine', '5-20 mcg/kg/min', 'Dose-dependent', 'Second-line for shock', 'Tachyarrhythmia; fallen out of favor'],
             ['Dobutamine', '5-20 mcg/kg/min', 'Inotropy', 'Low CO + adequate BP', 'Vasodilation, may drop BP'],
             ['Milrinone', '0.25-0.75 mcg/kg/min', 'Inotropy + vasodilation', 'Cardiogenic shock', 'Loading dose causes hypotension'],
             ['Vasopressin', '0.0003-0.002 U/kg/min', 'Vasoconstriction', 'Catecholamine-resistant', 'Digital ischemia'],
         ],
         'notes': 'Epinephrine is first-line for cold shock (most pediatric sepsis). Norepinephrine is first-line for warm shock. Dopamine has fallen out of favor due to arrhythmia risk. Milrinone is excellent for cardiogenic shock but causes vasodilation — be cautious in hypotensive patients. Vasopressin is a rescue agent for catecholamine-resistant shock.'},

        {'type': 'content', 'title': 'Cardiogenic Shock — A Different Beast',
         'bullets': [
             'Causes: myocarditis, cardiomyopathy, post-cardiac surgery, arrhythmia, CHD',
             'Recognition: hepatomegaly, JVD/periorbital edema, gallop rhythm, pulmonary crackles',
             'CXR: cardiomegaly, pulmonary edema, pleural effusions',
             'THE RULE: fluids WORSEN cardiogenic shock — small cautious bolus only (5-10 mL/kg)',
             'Management: inotropes (epinephrine or milrinone), diuretics if fluid-overloaded',
             'Point-of-care echo: decreased function, dilated chambers, pericardial effusion',
             'Consult cardiology and PICU early — may need mechanical support (ECMO)',
             '>If infant presents with "sepsis" but doesn\'t improve with fluids → think cardiogenic',
         ],
         'notes': 'Cardiogenic shock is the great mimicker. Infants with myocarditis present with tachypnea, poor feeding, and tachycardia — which looks identical to sepsis. The clue is hepatomegaly and worsening with fluid boluses. If a child who "looks septic" gets worse after 20-40 mL/kg of fluid, STOP and get a bedside echo. ECMO is the ultimate rescue for refractory cardiogenic shock.'},

        {'type': 'content', 'title': 'Obstructive Shock — Time-Critical Diagnoses',
         'bullets': [
             'TENSION PNEUMOTHORAX: absent breath sounds, tracheal deviation, distended neck veins',
             '>Treatment: immediate needle decompression (2nd ICS MCL) → chest tube',
             'CARDIAC TAMPONADE: Beck\'s triad (muffled hearts, JVD, hypotension), pulsus paradoxus',
             '>Treatment: pericardiocentesis; subxiphoid approach under US guidance',
             'MASSIVE PE: rare in children; acute RV failure, tachycardia, hypoxia',
             'DUCTAL-DEPENDENT CHD: neonate with cyanosis/shock; start PGE1 0.05-0.1 mcg/kg/min',
             'ALL require emergent treatment of the OBSTRUCTION, not just fluids',
         ],
         'notes': 'Obstructive shock requires removing the obstruction — fluids alone will not work. Tension pneumothorax is the most common and most treatable. In trauma, if a child suddenly deteriorates, think tension pneumo and decompress before waiting for CXR. Tamponade may be seen in penetrating trauma or pericarditis. Ductal-dependent CHD in neonates presents in the first 1-2 weeks of life as the ductus closes.'},

        {'type': 'content', 'title': 'Monitoring Endpoints of Resuscitation',
         'bullets': [
             'Heart rate: trending toward normal for age',
             'Capillary refill: improving toward <2 seconds',
             'Mental status: becoming more alert and interactive',
             'Urine output: >1 mL/kg/hr (catheterize for accurate measurement)',
             'Lactate: trending downward (clearance >10-20% at 2-4 hours)',
             'Point-of-care echo: improving cardiac function, IVC filling',
             'Blood pressure: normalizing (but remember this is a LATE indicator)',
             '>Reassess after EVERY intervention — this is an iterative process',
         ],
         'notes': 'Resuscitation is not a one-shot deal — it requires continuous reassessment. Lactate clearance is one of the best objective markers. A child whose lactate is dropping is responding to treatment. Mental status improvement is the most reassuring clinical sign. Build a habit of structured reassessment every 15-20 minutes during active resuscitation.'},

        {'type': 'case', 'title': '4-Year-Old with Fever and Poor Perfusion',
         'scenario': 'A 4-year-old girl presents with 3 days of fever, URI symptoms, and today became less active. She now won\'t walk. Vitals: HR 175, RR 34, BP 88/62, SpO2 96%, Temp 40.1°C, cap refill 4 seconds centrally, mottled extremities, weak peripheral pulses, GCS 13 (E3V4M6). Point-of-care glucose: 110 mg/dL. Weight: 18 kg.',
         'questions': [
             'What type of shock is this? (Distributive — septic shock, cold shock phenotype)',
             'First actions? (IV/IO, 20 mL/kg NS = 360 mL push, blood cultures, broad-spectrum antibiotics)',
             'After 60 mL/kg fluid (1080 mL), still HR 165 and cap refill 3 sec — next step?',
             'Which vasopressor and at what dose? (Epinephrine 0.1-0.3 mcg/kg/min for cold shock)',
         ],
         'notes': 'This is classic cold septic shock — tachycardia, prolonged cap refill, cool/mottled extremities. Note the BP is technically still normal for age. Walk the audience through each fluid bolus with reassessment. After 60 mL/kg, this is fluid-refractory shock needing vasopressors. Epinephrine is first-line for cold shock per ACCM guidelines.'},

        {'type': 'pitfalls', 'title': 'Shock Management Pitfalls',
         'items': [
             'Relying on blood pressure alone — hypotension is a LATE and OMINOUS sign',
             'Delayed IO access — 2 PIV attempts, 90 seconds, then drill without hesitation',
             'Giving >60 mL/kg without starting vasopressors — you are flooding the patient',
             'Aggressive fluids in cardiogenic shock — worsens pulmonary edema and function',
             'Not checking lactate — missing the severity of tissue hypoperfusion',
             'Forgetting to reassess after each bolus — resuscitation requires iteration',
             'Using dopamine first-line — epinephrine or norepinephrine are superior',
         ],
         'notes': 'Each of these pitfalls has caused patient harm. The BP trap is the most common — a child can have a normal BP and still be in severe compensated shock. The fluid overload pitfall applies to cardiogenic shock — always consider the type of shock before reflexively giving 60 mL/kg. And dopamine should no longer be first-line — it has more arrhythmias and less efficacy.'},
    ],
    'takeaways': [
        'Tachycardia with poor perfusion IS shock — do not wait for hypotension',
        'Identify the TYPE of shock before giving fluids — cardiogenic shock worsens with volume',
        'IO access within 90 seconds if PIV fails twice — do not delay resuscitation',
        'Fluid-refractory shock (>40-60 mL/kg): start vasopressors — epinephrine for cold, norepinephrine for warm',
        'Reassess after every intervention — shock management is an iterative process',
    ],
    'references': [
        'Davis AL, Carcillo JA, et al. ACCM Clinical Practice Parameters for Hemodynamic Support of Pediatric and Neonatal Septic Shock. Crit Care Med. 2017;45(6):1061-1093.',
        'Topjian AA, et al. Part 4: Pediatric BLS and ALS: 2020 AHA Guidelines. Circulation. 2020;142(16_suppl_2):S469-S523.',
        'Weiss SL, et al. Surviving Sepsis Campaign International Guidelines for Management of Septic Shock in Children. Pediatr Crit Care Med. 2020;21(2):e52-e106.',
        'Maitland K, et al. Mortality after fluid bolus in African children with severe infection (FEAST). N Engl J Med. 2011;364(26):2483-2495.',
        'de Caen AR, et al. Part 12: Pediatric Advanced Life Support: 2015 AHA Guidelines. Circulation. 2015;132(18_suppl_2):S526-S542.',
    ],
}

if __name__ == '__main__':
    build_deck(spec, os.path.join(os.path.dirname(__file__), '03_pediatric_shock.pptx'))

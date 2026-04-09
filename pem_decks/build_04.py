#!/usr/bin/env python3
"""Build Deck 4: Sepsis and Septic Shock in Children"""
import sys, os
sys.path.insert(0, os.path.dirname(__file__))
from build_deck import build_deck
from pem_design_system import TEAL, CORAL, NAVY, AMBER

spec = {
    'number': 4,
    'short_title': 'Sepsis & Septic Shock',
    'title': 'Sepsis and Septic Shock\nin Children',
    'subtitle': 'Recognition, The Golden Hour, Bundles, Vasopressors, and Escalation',
    'objectives': [
        'Apply pediatric-specific sepsis definitions and screening criteria',
        'Execute the sepsis bundle within the first hour: access, fluids, antibiotics',
        'Differentiate warm from cold septic shock and select appropriate vasopressors',
        'Recognize fluid-refractory and catecholamine-resistant shock',
        'Identify escalation thresholds to PICU and advanced therapies',
    ],
    'slides': [
        {'type': 'content', 'title': 'Pediatric Sepsis: Scope of the Problem',
         'bullets': [
             'Sepsis affects >72,000 children annually in the US; mortality 5-10% overall',
             'Septic shock mortality: 15-25% — higher in resource-limited settings',
             'EACH HOUR of delayed antibiotics increases mortality by ~8%',
             'Most pediatric sepsis deaths are potentially PREVENTABLE with early recognition',
             'High-risk groups: neonates, immunocompromised, chronic disease, asplenic',
             '>The "golden hour" concept: early bundled care within 60 min saves lives',
         ],
         'notes': 'Frame sepsis as a time-critical emergency analogous to stroke or STEMI. The data on antibiotic delay is compelling and should motivate rapid action. Many sepsis deaths occur because the diagnosis was not considered early enough. Screening tools help, but clinical suspicion remains the most important first step.'},

        {'type': 'table', 'title': 'Pediatric SIRS Criteria (Age-Specific)',
         'headers': ['Component', 'Criteria', 'Notes'],
         'rows': [
             ['Temperature', '>38.5°C or <36°C', 'Core temp preferred; hypothermia more ominous'],
             ['Heart Rate', '>2 SD above normal for age', 'See age-based HR table; or bradycardia if <1 yr'],
             ['Respiratory Rate', '>2 SD above normal for age', 'Or need for mechanical ventilation'],
             ['WBC', 'Elevated or depressed for age', 'Or >10% bands (immature forms)'],
             ['SEPSIS', 'SIRS + suspected/confirmed infection', '2 of 4 SIRS criteria required'],
             ['SEVERE SEPSIS', 'Sepsis + organ dysfunction', 'CV, respiratory, neuro, renal, hepatic, heme'],
             ['SEPTIC SHOCK', 'Sepsis + CV dysfunction', 'Despite ≥40 mL/kg fluid resuscitation'],
         ],
         'notes': 'Note that these are the traditional Goldstein 2005 criteria. Newer approaches (Phoenix Sepsis Score 2024) are moving toward organ dysfunction-based definitions similar to adult Sepsis-3. However, SIRS criteria remain widely used for screening. The key point is that sepsis definitions in children are age-dependent — what is normal at 2 months is abnormal at 10 years.'},

        {'type': 'algorithm', 'title': 'The Sepsis Bundle: First 60 Minutes',
         'steps': [
             '0-5 min: Recognize sepsis → move to resus bay → apply monitors',
             '0-5 min: Establish IV/IO access → draw blood cultures + labs → CHECK GLUCOSE',
             '0-15 min: Start 20 mL/kg NS push → give empiric ANTIBIOTICS immediately',
             '15-30 min: Reassess → repeat 20 mL/kg if needed (up to 60 mL/kg)',
             '30-60 min: If fluid-refractory → START VASOPRESSORS (peripheral or IO ok to start)',
             '60 min: Reassess endpoints → admit PICU if vasoactive agents needed',
         ],
         'notes': 'This bundle is the core of sepsis management. Antibiotics and fluids within the first hour are the two interventions with the strongest evidence for reducing mortality. Do NOT delay antibiotics waiting for cultures — draw them and give antibiotics simultaneously. Vasopressors can be started through a peripheral IV or IO while central access is being obtained.'},

        {'type': 'table', 'title': 'Empiric Antibiotic Selection by Age',
         'headers': ['Age Group', 'First-Line', 'If Concern For', 'Add'],
         'rows': [
             ['0-28 days', 'Ampicillin + Gentamicin', 'HSV', 'Acyclovir 20 mg/kg IV q8h'],
             ['29-60 days', 'Ampicillin + Cefotaxime (or Ceftriaxone if >28d)', 'HSV, UTI source', 'Acyclovir if indicated'],
             ['61 days - 3 years', 'Ceftriaxone 100 mg/kg (max 4g)', 'MRSA', 'Vancomycin 15 mg/kg'],
             ['3-18 years', 'Ceftriaxone 100 mg/kg (max 4g)', 'MRSA, pseudomonal', 'Vancomycin ± piperacillin-tazobactam'],
             ['Immunocompromised', 'Cefepime or meropenem', 'Fungal', 'Consider antifungals'],
             ['Intra-abdominal', 'Piperacillin-tazobactam or meropenem', 'Anaerobes', 'Metronidazole if not covered'],
         ],
         'notes': 'Antibiotic selection depends on age, suspected source, and local resistance patterns. Ampicillin covers Listeria and Group B Strep in neonates. Ceftriaxone is the workhorse for older children. Always add vancomycin if MRSA is prevalent in your community or the child has central line, recent hospitalization, or skin/soft tissue source. Give antibiotics within 60 minutes — ideally within 30.'},

        {'type': 'two_column', 'title': 'Warm vs Cold Septic Shock Management',
         'left_title': 'WARM SHOCK',
         'left': [
             'Vasodilated: warm, flushed, bounding pulses',
             'Flash capillary refill',
             'Wide pulse pressure',
             'Often early presentation',
             'FIRST-LINE: Norepinephrine',
             'Start: 0.05 mcg/kg/min, titrate',
             'Goal: improve SVR',
             'If refractory: add vasopressin 0.0003-0.002 U/kg/min',
         ],
         'right_title': 'COLD SHOCK (More Common in Children)',
         'right': [
             'Vasoconstricted: cool, mottled, thready pulses',
             'Prolonged capillary refill >3 sec',
             'Narrow pulse pressure',
             'More common pediatric presentation',
             'FIRST-LINE: Epinephrine',
             'Start: 0.05-0.1 mcg/kg/min, titrate',
             'Goal: improve cardiac output',
             'If refractory: add milrinone or dobutamine',
         ],
         'notes': 'This is one of the most important clinical distinctions in PEM. Most children present with cold shock (60-70%), unlike adults who more commonly present with warm shock. Cold shock implies poor cardiac output — epinephrine provides both inotropy and chronotropy. If cold shock persists despite epinephrine, adding milrinone can improve cardiac output through lusitropy and afterload reduction.'},

        {'type': 'content', 'title': 'Fluid-Refractory Shock: When Fluids Aren\'t Enough',
         'bullets': [
             'Definition: shock persisting despite 40-60 mL/kg isotonic crystalloid',
             'Occurs in approximately 30% of children with septic shock',
             'START VASOPRESSORS — do not continue giving fluid boluses hoping they\'ll work',
             'Can start epinephrine/norepinephrine through PERIPHERAL IV or IO',
             '>Peripheral vasopressor use is safe for short duration during stabilization',
             'Obtain central venous access (femoral preferred in emergencies)',
             'Consider bedside echo: assess cardiac function, IVC, pericardial effusion',
             'Reassess for OTHER causes: tension pneumo, tamponade, adrenal crisis, toxin',
         ],
         'notes': 'Fluid-refractory shock is a critical inflection point. The old dogma of requiring central access before starting pressors has been replaced by evidence showing short-term peripheral vasopressor use is safe and life-saving. Push diluted epinephrine through a peripheral IV while obtaining central access. Bedside echo at this point is invaluable to guide further management.'},

        {'type': 'content', 'title': 'Catecholamine-Resistant Shock',
         'bullets': [
             'Shock persisting despite epinephrine AND norepinephrine at adequate doses',
             'Mortality rises significantly at this stage',
             'Consider STRESS-DOSE HYDROCORTISONE:',
             '>Hydrocortisone 2 mg/kg IV bolus, then 2 mg/kg/day divided q6h',
             '>Indicated if suspected adrenal insufficiency, chronic steroid use, or purpura fulminans',
             'Consider vasopressin 0.0003-0.002 U/kg/min as adjunct',
             'Consider ECMO for refractory septic shock in centers with capability',
             'Definitely in PICU at this point — this is ICU-level management',
         ],
         'notes': 'Catecholamine-resistant shock is a bad place to be. Stress-dose hydrocortisone is recommended when shock persists despite adequate vasopressor doses, particularly if there is concern for adrenal insufficiency. The SPROUT study and other data support early use. ECMO for refractory septic shock is available at major pediatric centers and should be considered before the child is too far gone.'},

        {'type': 'content', 'title': 'Laboratory Evaluation in Sepsis',
         'bullets': [
             'BLOOD CULTURES: draw before antibiotics if possible — but do NOT delay antibiotics',
             'LACTATE: point-of-care; >2 = concerning, >4 = severe tissue hypoperfusion',
             'CBC: WBC extremes (high or low), left shift, thrombocytopenia (DIC marker)',
             'CMP: renal function, electrolytes, glucose, liver enzymes',
             'COAGULATION: PT/INR, fibrinogen — DIC screening',
             'PROCALCITONIN: >0.5 ng/mL supports bacterial infection; rising trend ominous',
             'BLOOD GAS: metabolic acidosis (base deficit), respiratory compensation',
             'URINALYSIS + CULTURE: UTI is a common source in young children',
         ],
         'notes': 'Labs guide management but should never delay treatment. The most important tests are blood cultures (to guide de-escalation), lactate (to assess severity), and glucose (to treat immediately). Procalcitonin is increasingly useful for identifying bacterial sepsis and monitoring response to treatment. A rising procalcitonin despite treatment is a red flag.'},

        {'type': 'key_point', 'title': 'Clinical Pearl',
         'point': 'Every hour of antibiotic delay increases sepsis mortality by ~8%.',
         'sub': 'Draw cultures and give antibiotics simultaneously. Do not delay antibiotics for the LP in a sick-appearing child. The LP can wait; the antibiotics cannot.',
         'notes': 'This is the single most important message for sepsis management. Kumar et al. and subsequent pediatric studies show a clear relationship between time to antibiotics and mortality. In the real world, the most common delay is waiting for cultures, LP, or the "right" antibiotic choice. Give a reasonable broad-spectrum agent NOW and refine later.'},

        {'type': 'case', 'title': '6-Week-Old with Fever and Poor Feeding',
         'scenario': 'A 6-week-old presents to the ED with a rectal temperature of 38.8°C, poor feeding for 12 hours, and "just not acting right." Mom reports one less wet diaper than usual. On exam: HR 195, RR 52, BP 62/38, SpO2 97%, cap refill 4 seconds, mottled trunk and extremities, lethargic but rousable. Weight 4.5 kg.',
         'questions': [
             'What is your immediate assessment? (Decompensated septic shock in a young infant)',
             'First 5 minutes? (IV/IO, glucose check, 20 mL/kg NS = 90 mL push, labs + cultures)',
             'Antibiotic choice? (Ampicillin 50 mg/kg + Cefotaxime 50 mg/kg + Acyclovir 20 mg/kg)',
             'Why acyclovir? (HSV must be covered in febrile neonates with sepsis-like presentation)',
         ],
         'notes': 'This case drives home several critical points: young infants with fever and poor perfusion are septic until proven otherwise. The antibiotic regimen must cover neonatal pathogens including HSV. Acyclovir is added because HSV encephalitis/disseminated disease is devastating and can present exactly like bacterial sepsis. You cannot clinically distinguish them — so cover both.'},

        {'type': 'pitfalls', 'title': 'Sepsis Management Pitfalls',
         'items': [
             'Missing compensated septic shock — tachycardia + poor perfusion + normal BP = SHOCK',
             'Delaying antibiotics for LP, cultures, or "the right antibiotic" — just give them NOW',
             'Giving inadequate fluid volumes — many providers stop at 20-40 mL/kg too early',
             'Not starting vasopressors when fluid-refractory — pressors save lives',
             'Forgetting to cover HSV in neonates (acyclovir with ampicillin + cefotaxime)',
             'Not repeating lactate to monitor clearance — trending is more useful than a single value',
             'Discharging a child who "looks better" after fluid bolus without adequate observation',
         ],
         'notes': 'Walk through each pitfall with clinical examples. The compensated shock trap is the most common — a child with HR 180, cap refill 4 seconds, and BP 90/60 is in shock even though the BP is "normal." The antibiotic delay pitfall kills patients. And the discharge pitfall is real — children can transiently improve with fluids and then decompensate at home.'},
    ],
    'takeaways': [
        'Sepsis is a TIME-CRITICAL emergency — antibiotics and fluids within 60 minutes',
        'Most children with septic shock present with COLD shock — epinephrine is first-line',
        'Fluid-refractory shock needs vasopressors — do not keep bolusing past 60 mL/kg',
        'Cover HSV with acyclovir in all neonates and young infants with sepsis',
        'Reassess continuously — sepsis management is an iterative process, not a single intervention',
    ],
    'references': [
        'Weiss SL, et al. Surviving Sepsis Campaign International Guidelines for the Management of Septic Shock and Sepsis-Associated Organ Dysfunction in Children. Pediatr Crit Care Med. 2020;21(2):e52-e106.',
        'Davis AL, Carcillo JA, et al. ACCM Clinical Practice Parameters for Hemodynamic Support of Pediatric and Neonatal Septic Shock. Crit Care Med. 2017;45(6):1061-1093.',
        'Schlapbach LJ, et al. International consensus criteria for pediatric sepsis and septic shock (Phoenix). JAMA. 2024;331(8):665-674.',
        'Paul R, et al. Improving adherence to PALS septic shock guidelines. Pediatrics. 2014;133(5):e1358-e1366.',
        'Evans IVR, et al. Association between the NY sepsis care mandate and in-hospital mortality. JAMA. 2018;320(4):358-367.',
        'Kumar A, et al. Duration of hypotension before initiation of effective antimicrobial therapy is the critical determinant of survival. Crit Care Med. 2006;34(6):1589-1596.',
    ],
}

if __name__ == '__main__':
    build_deck(spec, os.path.join(os.path.dirname(__file__), '04_sepsis_septic_shock.pptx'))

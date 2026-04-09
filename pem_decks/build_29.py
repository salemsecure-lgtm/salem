#!/usr/bin/env python3
"""Build Deck 29: Neonatal Emergencies in the ED"""
import sys, os
sys.path.insert(0, os.path.dirname(__file__))
from build_deck import build_deck

spec = {
    'number': 29,
    'short_title': 'Neonatal Emergencies',
    'title': 'Neonatal Emergencies in the ED',
    'subtitle': 'Critical Presentations, Resuscitation, and Must-Not-Miss Diagnoses in the First 28 Days',
    'objectives': [
        'Recognize the critically ill neonate — key presentations that demand immediate action',
        'Differentiate cardiac, infectious, metabolic, and surgical causes of neonatal collapse',
        'Initiate neonatal resuscitation and stabilization in the ED',
        'Identify ductal-dependent cardiac disease, inborn errors of metabolism, and neonatal sepsis',
        'Understand the unique physiology of the neonate that impacts ED management',
    ],
    'slides': [
        {'type': 'content', 'title': 'The Crashing Neonate: A PEM Emergency',
         'bullets': [
             'Neonates (0-28 days) who present to the ED sick are often VERY sick',
             'Neonatal physiology is unique: transitional circulation, immature immune system, limited reserves',
             'The differential for the sick neonate is broad but can be organized into key categories',
             'Categories: Sepsis/Infection, Cardiac (ductal-dependent CHD), Metabolic (IEM), NAT, Surgical',
             'Key initial assessment: does this neonate need resuscitation? → ABCs first',
             '>The sick neonate is the highest-acuity patient in the PED — treat aggressively while you investigate',
         ],
         'notes': 'Neonates who come to the ED sick deserve the highest level of concern. They have limited physiologic reserve and can decompensate rapidly. The differential is broad, but a structured approach helps. Always think of the 5 major categories: sepsis, cardiac, metabolic, abuse, and surgical. Start treatment (antibiotics, PGE1, glucose) before you have a definitive diagnosis.'},

        {'type': 'algorithm', 'title': 'Approach to the Sick Neonate',
         'steps': [
             'ABCs: ensure airway patency, adequate breathing, assess perfusion and HR',
             'GLUCOSE: check immediately — treat hypoglycemia with D10W 2-5 mL/kg IV',
             'TEMPERATURE: neonates cannot thermoregulate — warm aggressively, check rectal temp',
             'EMPIRIC TREATMENT: ampicillin + gentamicin + acyclovir (if HSV concern); prostaglandin if cardiac suspected',
             'FOCUSED HISTORY: birth history, GBS status, maternal STI history, feeding pattern, last well time',
             'WORKUP: CBC, CMP, VBG, blood culture, UA/UCx, LP (if able), LFTs, ammonia, lactate, cortisol',
             'IMAGING: CXR (heart size, lung fields), 4-extremity BP and pre/post-ductal SpO2 if cardiac concern',
         ],
         'notes': 'This algorithm is the framework for every sick neonate in the ED. The glucose check is #2 after ABCs because neonatal hypoglycemia can cause seizures and permanent brain injury. Temperature management is critical — a cold neonate becomes acidotic and coagulopathic. Empiric broad-spectrum antibiotics should be given within the first 30 minutes. Start PGE1 if cardiac disease is suspected.'},

        {'type': 'table', 'title': 'Differentiating the Cause of Neonatal Collapse',
         'headers': ['Feature', 'Sepsis', 'Cardiac (CHD)', 'Metabolic (IEM)', 'NAT'],
         'rows': [
             ['Onset', 'Gradual (hours-days)', 'Days 2-14 (as ductus closes)', 'Days 2-7 (after feeding starts)', 'Acute (minutes)'],
             ['Temp', 'Fever or hypothermia', 'Usually afebrile', 'Usually afebrile', 'Variable'],
             ['Feeding', 'Poor feeding, lethargy', 'Poor feeding, diaphoresis with feeds', 'Vomiting, poor feeding', 'Abrupt change'],
             ['Cardiovascular', 'Tachycardia, poor perfusion', 'Cyanosis OR shock; murmur, hepatomegaly', 'Usually normal initially', 'Variable'],
             ['Key Lab', 'WBC abnormal, CRP/PCT elevated', 'Pre/post-ductal SpO2 difference', 'Ammonia, lactate, anion gap acidosis', 'CT head, skeletal survey'],
             ['Key Treatment', 'Antibiotics + acyclovir', 'PGE1', 'Glucose + correct acidosis + stop protein', 'Stabilize, protect, report'],
         ],
         'notes': 'This table helps differentiate the 4 major causes of neonatal collapse. The timing of presentation is a critical clue: ductal-dependent cardiac disease typically presents at day 2-14 as the ductus closes. IEM typically presents when protein catabolism increases (after feeding is established). Sepsis can present at any time. NAT is acute and often without clear preceding illness.'},

        {'type': 'content', 'title': 'Ductal-Dependent Cardiac Disease (Review)',
         'bullets': [
             'Ductal-dependent lesions require a patent ductus arteriosus for survival',
             'Systemic flow dependent: coarctation, critical AS, HLHS, interrupted aortic arch → present with SHOCK',
             'Pulmonary flow dependent: critical PS, pulmonary atresia, tricuspid atresia, severe TOF → present with CYANOSIS',
             'Key test: pre- and post-ductal SpO2; >3% difference suggests ductal-dependent lesion',
             'Hyperoxia test: if SpO2 does not improve with 100% FiO2 → cardiac cause (intracardiac shunting)',
             'TREATMENT: PGE1 0.05-0.1 mcg/kg/min IV — reopens the ductus',
             'PGE1 side effects: apnea (30-40%), fever, hypotension — be prepared to intubate',
             '>Start PGE1 and call cardiology — do not wait for echo to confirm the diagnosis',
         ],
         'notes': 'This is a review of the ductal-dependent cardiac disease content from the cardiac deck, applied specifically to the neonatal context. The key clinical scenario: a neonate who looked well at birth becomes progressively cyanotic or develops shock-like symptoms at day 2-14. If supplemental oxygen does not improve SpO2, this is cardiac until proven otherwise. Start PGE1.'},

        {'type': 'content', 'title': 'Inborn Errors of Metabolism: The Metabolic Emergency',
         'bullets': [
             'IEM present in the neonatal period when toxic metabolites accumulate after feeding begins',
             'Classic presentation: well at birth → progressive lethargy, poor feeding, vomiting at day 2-7',
             'Key labs: ammonia (>200 µmol/L is very concerning), lactate, pH/VBG, glucose, anion gap, urine ketones',
             'Elevated ammonia + respiratory alkalosis = urea cycle defect (most common severe IEM)',
             'Lactic acidosis + hypoglycemia = organic acidemias or fatty acid oxidation defects',
             'Treatment: STOP all protein intake immediately, give D10 + electrolytes at high rate (prevent catabolism)',
             'For hyperammonemia: sodium benzoate + sodium phenylbutyrate; consider hemodialysis if ammonia >500',
             '>If ammonia is >200 in a neonate, call genetics and PICU immediately',
         ],
         'notes': 'IEM is the most commonly missed cause of neonatal collapse because it mimics sepsis. The key differentiator: a well neonate who becomes progressively ill without an obvious infectious source and who has metabolic derangements (high ammonia, severe acidosis, hypoglycemia). Ammonia is the single most important lab to order in any neonate with unexplained AMS, seizures, or progressive deterioration.'},

        {'type': 'two_column', 'title': 'Neonatal Resuscitation in the ED (Non-Delivery)',
         'left_title': 'KEY DIFFERENCES FROM OLDER CHILDREN',
         'left': [
             'Airway: larger occiput → sniffing position with shoulder roll',
             'ETT size: 3.0-3.5 cuffed for term neonate',
             'Compressions: 2-thumb encircling technique; 3:1 ratio (not 15:2)',
             'Epinephrine: 0.01-0.03 mg/kg IV/IO (1:10,000)',
             'Volume: 10 mL/kg NS bolus (not 20 mL/kg)',
             'Temperature: aggressive warming — radiant warmer, warm room, plastic wrap for preterm',
             'Glucose: D10W 2-5 mL/kg (not D25W — risk of hyperglycemia and osmotic injury)',
         ],
         'right_title': 'COMMON NEONATAL RESUSCITATION PITFALLS',
         'right': [
             'Using adult-size equipment',
             'Forgetting the shoulder roll (occiput is proportionally large)',
             'Giving D25W or D50W — too concentrated; use D10W in neonates',
             'Not checking glucose early',
             'Over-resuscitation with fluids (10 mL/kg boluses, not 20)',
             'Not starting antibiotics empirically',
             'Forgetting to maintain temperature',
         ],
         'notes': 'Neonatal resuscitation in the ED follows NRP guidelines but adapted for the post-delivery context. The compression ratio is 3:1 (for neonates — if primary cardiac arrest is suspected, use 15:2). Volume resuscitation is more conservative — 10 mL/kg boluses. D10W is the standard glucose replacement (D25W can cause osmotic injury to immature brain vasculature). Temperature management is critical.'},

        {'type': 'content', 'title': 'Neonatal Jaundice: When It Is an Emergency',
         'bullets': [
             'Physiologic jaundice: peaks at day 3-5, resolves by day 14 — COMMON and usually benign',
             'Pathologic jaundice red flags:',
             '>Jaundice within first 24 hours of life (hemolytic disease)',
             '>Total bilirubin >95th percentile for age (use Bhutani nomogram)',
             '>Rate of rise >0.2 mg/dL/hour (rapidly rising)',
             '>Conjugated (direct) bilirubin >1 mg/dL or >20% of total (biliary atresia, sepsis)',
             'Kernicterus: bilirubin-induced brain injury — devastating and preventable',
             'Treatment: phototherapy (most cases); exchange transfusion if critically elevated',
             '>Any neonate <24 hours old with visible jaundice needs URGENT bilirubin measurement',
         ],
         'notes': 'Most neonatal jaundice is benign, but severe hyperbilirubinemia can cause kernicterus (permanent neurologic damage). The key is recognizing when jaundice is pathologic. Plot the bilirubin on the Bhutani nomogram for age in hours. Conjugated hyperbilirubinemia is NEVER physiologic and suggests biliary atresia (surgical emergency), hepatitis, or sepsis.'},

        {'type': 'case', 'title': '10-Day-Old with Grunting and Poor Feeding',
         'scenario': 'A 10-day-old full-term male presents with progressive lethargy and poor feeding over 24 hours. He was feeding well until yesterday. Born via uncomplicated vaginal delivery, GBS negative. On exam: gray, mottled, grunting, HR 190, RR 60, BP 55/30 (left arm), SpO2 78% (right hand), 65% (left foot), weak femoral pulses. Temperature 36.2°C (hypothermic).',
         'questions': [
             'What is the pre/post-ductal SpO2 differential? (78% pre-ductal vs 65% post-ductal = >3% difference → ductal-dependent)',
             'Most likely diagnosis? (Coarctation of the aorta or critical left-sided obstructive lesion → ductus is closing)',
             'Immediate management? (PGE1 0.05 mcg/kg/min IV, prepare for intubation, NS 10 mL/kg bolus for shock)',
             'Also: antibiotics (cannot exclude sepsis), glucose check, warm the baby, PICU and cardiology STAT',
         ],
         'notes': 'This is the classic ductal-dependent cardiac collapse case. The timing (day 10), the pre/post-ductal SpO2 difference, the weak femoral pulses, and the shock picture all point to a left-sided obstructive lesion (likely coarctation or interrupted aortic arch). PGE1 is life-saving here — it reopens the ductus and restores systemic blood flow. Start it immediately.'},

        {'type': 'pitfalls', 'title': 'Neonatal Emergency Pitfalls',
         'items': [
             'Not checking glucose in every sick neonate — hypoglycemia is common, treatable, and causes brain injury',
             'Attributing neonatal collapse to "sepsis" without considering cardiac or metabolic causes — workup broadly',
             'Not starting PGE1 when cardiac disease is suspected — do not wait for the echo',
             'Using D25W or D50W in neonates — use D10W only to prevent osmotic brain injury',
             'Forgetting to check ammonia in unexplained neonatal AMS or seizures — IEM is commonly missed',
             'Not maintaining temperature — hypothermia causes acidosis, coagulopathy, and worsens outcomes',
             'Discharging a jaundiced neonate <24 hours old without measuring bilirubin',
         ],
         'notes': 'These pitfalls capture the most common and most consequential errors in neonatal emergency medicine. The glucose and temperature points are the most fundamental. The PGE1 and ammonia points are the most commonly missed. The bilirubin point prevents kernicterus. Each of these can be life-saving or brain-saving when applied correctly.'},
    ],
    'takeaways': [
        'The sick neonate differential: Sepsis, Cardiac, Metabolic, NAT, Surgical — workup broadly',
        'Check glucose and temperature IMMEDIATELY in every sick neonate',
        'PGE1 (0.05-0.1 mcg/kg/min) for suspected ductal-dependent cardiac disease — do not wait for echo',
        'Check ammonia in any neonate with unexplained AMS, seizures, or progressive deterioration',
        'Empiric antibiotics (ampicillin + gentamicin ± acyclovir) within 30 minutes for any sick neonate',
    ],
    'references': [
        'Defined by Weiner GM, ed. Textbook of Neonatal Resuscitation (NRP). 8th ed. AAP; 2021.',
        'Defined by Kemper AR, et al. Strategies for implementing screening for critical congenital heart disease. Pediatrics. 2011;128(5):e1259-e1267.',
        'Defined by Leonard JV, Morris AA. Diagnosis and early management of inborn errors of metabolism presenting around the time of birth. Acta Paediatr. 2006;95(1):6-14.',
        'Defined by AAP Subcommittee on Hyperbilirubinemia. Management of hyperbilirubinemia in the newborn infant 35 or more weeks of gestation. Pediatrics. 2004;114(1):297-316.',
        'Defined by Baird JS, et al. Neonatal emergencies. Pediatr Rev. 2022;43(5):249-261.',
    ],
}

if __name__ == '__main__':
    build_deck(spec, os.path.join(os.path.dirname(__file__), '29_neonatal_emergencies.pptx'))

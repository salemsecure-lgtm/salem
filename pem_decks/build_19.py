#!/usr/bin/env python3
"""Build Deck 19: Fever in Neonates and Young Infants"""
import sys, os
sys.path.insert(0, os.path.dirname(__file__))
from build_deck import build_deck

spec = {
    'number': 19,
    'short_title': 'Febrile Infant',
    'title': 'Fever in Neonates and Young Infants',
    'subtitle': 'Risk Stratification, Evidence-Based Workup, and the REVISE Protocol',
    'objectives': [
        'Apply age-based risk stratification for febrile infants: 0-28 days, 29-60 days, 61-90 days',
        'Execute the appropriate workup for each age group including labs, UA, and LP indications',
        'Understand the PECARN Febrile Infant prediction rules and Step-by-Step approach',
        'Select empiric antibiotics based on age and risk level',
        'Make evidence-based admission vs. discharge decisions for febrile infants',
    ],
    'slides': [
        {'type': 'content', 'title': 'The Febrile Infant: A PEM Core Topic',
         'bullets': [
             'Fever = rectal temperature ≥38.0°C (100.4°F) — rectal is the gold standard',
             'Young febrile infants cannot localize infection and have immature immune systems',
             'Risk of serious bacterial infection (SBI) varies dramatically by age:',
             '>0-28 days: ~10-12% risk of SBI — always warrants full sepsis workup',
             '>29-60 days: ~5-7% risk of SBI — risk stratification possible',
             '>61-90 days: ~2-3% risk of SBI — lower risk, more selective workup',
             'SBI includes: UTI (most common), bacteremia, meningitis',
             '>An ill-appearing febrile infant of any age gets a full sepsis workup and empiric antibiotics immediately',
         ],
         'notes': 'This is one of the most tested and most important PEM topics. The approach to the febrile infant has evolved significantly with prediction rules (PECARN, Rochester, Step-by-Step). But the foundational principle remains: young infants with fever can have occult serious infections that are not apparent on exam. Risk stratification helps, but clinical judgment is paramount.'},

        {'type': 'algorithm', 'title': 'Approach by Age Group',
         'steps': [
             '0-28 DAYS: FULL SEPSIS WORKUP — CBC, blood culture, UA/UCx, LP, ± CXR; empiric ampicillin + gentamicin (+ acyclovir if HSV concern); ADMIT all',
             '29-60 DAYS: Risk stratify — well-appearing → labs (CBC, UA, procalcitonin/CRP); if LOW RISK → consider discharge with close follow-up and IM ceftriaxone; if HIGH RISK → LP + admit + antibiotics',
             '61-90 DAYS: Lower risk — well-appearing + normal UA + normal inflammatory markers → can consider outpatient management; abnormal → further workup ± admission',
         ],
         'notes': 'The 0-28 day old always gets a full workup and admission — there is no controversy here. The decision points are in the 29-60 day range, where prediction rules help. The 61-90 day old well-appearing infant with a normal UA and normal inflammatory markers is at very low risk and may not need LP or admission. But this requires reliable follow-up within 24 hours.'},

        {'type': 'table', 'title': 'PECARN Febrile Infant Rule (29-60 Days)',
         'headers': ['Criterion', 'Low Risk If', 'Action If Low Risk', 'Action If High Risk'],
         'rows': [
             ['Urinalysis', 'Negative (no pyuria, no leukocyte esterase, no nitrites)', 'Continue assessment', 'Full workup + LP + admit'],
             ['ANC', '<4,090/µL', 'Continue assessment', 'Full workup + LP + admit'],
             ['Procalcitonin', '<0.5 ng/mL', 'LOW RISK — no LP needed', 'Full workup + LP + admit'],
         ],
         'notes': 'The PECARN rule for 29-60 day old well-appearing febrile infants uses three biomarkers sequentially: UA, ANC, and procalcitonin. If ALL three are negative, the risk of SBI is <1% and the risk of bacterial meningitis is ~0.1%. These infants may be managed without LP and potentially without admission. Key: procalcitonin is essential — CRP alone is not sufficient for this rule.'},

        {'type': 'content', 'title': 'The Lumbar Puncture Question',
         'bullets': [
             '0-28 days: LP is ALWAYS indicated — meningitis risk is highest in this age group',
             '29-60 days: LP indicated if ANY high-risk marker (abnormal UA, elevated ANC, elevated procalcitonin)',
             '29-60 days, low risk by PECARN: LP can be safely deferred with close follow-up',
             '61-90 days: LP indicated if ill-appearing, abnormal labs, or clinical concern',
             'NEVER delay antibiotics for LP — if the child is ill, give antibiotics FIRST',
             'Failed LP does not mean you cannot treat — give empiric antibiotics and consider repeat LP later',
             '>The purpose of LP is to rule out meningitis, not to delay treatment',
         ],
         'notes': 'The LP question generates the most anxiety. The key message: LP is critical in the youngest infants and when clinical suspicion is high. But it should NEVER delay antibiotics in an ill-appearing child. If LP fails (common in neonates), treat empirically and consider traumatic LP interpretation or repeat LP in 24-48 hours if clinically improving.'},

        {'type': 'content', 'title': 'HSV: The Can\'t-Miss Neonatal Diagnosis',
         'bullets': [
             'HSV meningoencephalitis: mortality up to 30% if untreated; severe neurologic sequelae in survivors',
             'Risk factors: maternal HSV (but 60-80% of neonatal HSV occurs without known maternal history)',
             'Peak presentation: 7-21 days of life (can present up to 42 days)',
             'Three forms: SEM (skin, eye, mouth), CNS, disseminated',
             'Clues: vesicles (present in only 40%), seizures, elevated AST/ALT, CSF pleocytosis, thrombocytopenia, coagulopathy',
             'When to add acyclovir: any neonate ≤21 days with fever, seizures, vesicles, ill-appearing, CSF pleocytosis, elevated LFTs, maternal HSV history',
             'Dose: acyclovir 20 mg/kg IV q8h until HSV PCR results available',
             '>If in doubt about HSV, START ACYCLOVIR. The risk of NOT treating far exceeds the risk of treatment.',
         ],
         'notes': 'HSV is the diagnosis that haunts every PEM physician. It is rare but devastating if missed. The threshold for starting acyclovir should be LOW. Many experts recommend acyclovir for any ill-appearing neonate ≤21 days, even without specific HSV risk factors. The CSF HSV PCR takes 24-48 hours — do not wait for results to start treatment. Also send surface cultures (mouth, eyes, rectum) and serum HSV PCR.'},

        {'type': 'table', 'title': 'Empiric Antibiotic Selection by Age',
         'headers': ['Age', 'Antibiotics', 'Dosing', 'Coverage'],
         'rows': [
             ['0-28 days', 'Ampicillin + Gentamicin', 'Amp 50 mg/kg q6-8h + Gent 4-5 mg/kg q24h', 'GBS, E. coli, Listeria; add acyclovir 20 mg/kg q8h if HSV concern'],
             ['29-60 days', 'Ceftriaxone ± Ampicillin', 'Ceftriaxone 50 mg/kg q24h; Amp 50 mg/kg q6h', 'Add ampicillin if Listeria concern or ≤30-day adjusted age'],
             ['61-90 days (admit)', 'Ceftriaxone', '50 mg/kg IV q24h', 'S. pneumo, E. coli, other GNR, GBS'],
             ['61-90 days (outpatient)', 'Ceftriaxone IM', '50 mg/kg IM × 1 dose', 'Bridge until 24h follow-up and culture results'],
         ],
         'notes': 'Key nuance: ampicillin is added for Listeria coverage. Listeria is rare but can cause devastating meningitis in neonates, and cephalosporins do NOT cover Listeria. Some experts continue ampicillin through 60 days. The decision to use ceftriaxone IM for outpatient management of low-risk 29-60 day olds requires reliable 24-hour follow-up and negative preliminary cultures.'},

        {'type': 'two_column', 'title': 'Admit vs. Discharge: Febrile Infant',
         'left_title': 'MUST ADMIT',
         'left': [
             'All febrile neonates 0-28 days',
             'Ill-appearing infant of any age',
             'Abnormal inflammatory markers (ANC ≥4,090, PCT ≥0.5)',
             'Positive urinalysis in <60 days',
             'CSF pleocytosis or abnormal CSF',
             'Any concern for HSV, meningitis, or bacteremia',
             'Unreliable follow-up or social concerns',
             'Premature infant or immunocompromised',
         ],
         'right_title': 'CONSIDER OUTPATIENT (29-60d only)',
         'right': [
             'Well-appearing by validated assessment',
             'Normal UA (no pyuria, no LE, no nitrites)',
             'ANC <4,090/µL',
             'Procalcitonin <0.5 ng/mL',
             'No CSF abnormalities (if LP performed)',
             'Reliable caregiver with phone',
             'Follow-up available within 24 hours',
             'IM ceftriaxone given before discharge',
         ],
         'notes': 'Outpatient management of low-risk febrile infants 29-60 days is increasingly supported by evidence but requires: 1) strict adherence to prediction rule criteria, 2) IM ceftriaxone given before discharge, 3) reliable follow-up within 24 hours, 4) clear return precautions. This is an institutional and attending-level decision — know your local practice.'},

        {'type': 'case', 'title': '21-Day-Old with Fever 38.4°C',
         'scenario': 'A 21-day-old full-term male is brought in for a rectal temperature of 38.4°C at home. He is breastfeeding well. Mother had an uncomplicated vaginal delivery, was GBS negative, no maternal HSV history. The infant appears well: alert, active, good color, normal tone. No rash or vesicles.',
         'questions': [
             'Is this infant low risk? (NO — age ≤28 days = full sepsis workup regardless of appearance)',
             'Required workup? (CBC, blood culture, UA/UCx, LP with CSF culture/cell count/gram stain/glucose/protein, ± CXR)',
             'Should you add acyclovir? (Consider: age 21 days = within HSV risk window; if well-appearing and no HSV risk factors, may defer; if ANY concern → add acyclovir)',
             'Empiric antibiotics? (Ampicillin + Gentamicin; add acyclovir if HSV concern)',
             'Disposition? (ADMIT — all febrile neonates ≤28 days are admitted)',
         ],
         'notes': 'This is the bread-and-butter febrile neonate case. Despite looking well, this infant needs a full workup and admission. The well appearance does NOT rule out SBI in this age group. The HSV question is nuanced: at 21 days of life, the infant is within the peak HSV window. Many experts would add acyclovir pending workup results. Send HSV PCR on CSF and surface cultures.'},

        {'type': 'pitfalls', 'title': 'Febrile Infant Pitfalls',
         'items': [
             'Relying on appearance alone — well-appearing neonates can have occult bacteremia or meningitis',
             'Not doing LP in febrile neonates ≤28 days — LP is ALWAYS indicated in this age group',
             'Delaying antibiotics while waiting for LP — if the infant is ill, give antibiotics FIRST',
             'Forgetting HSV — add acyclovir for any concerning neonate ≤21 days (vesicles, seizures, elevated LFTs)',
             'Using CRP instead of procalcitonin for PECARN rule — procalcitonin is the validated biomarker',
             'Accepting a "normal UA" by dipstick alone — send formal UA with microscopy and UCx',
             'Discharging a 29-60 day old without ensuring 24-hour follow-up and return precautions',
         ],
         'notes': 'The HSV pitfall is the most devastating. The CRP vs. procalcitonin point is important: the PECARN rule specifically validated procalcitonin, not CRP. If your lab does not offer procalcitonin, you cannot apply the PECARN rule as designed, and a more conservative approach (LP + admission) is warranted.'},
    ],
    'takeaways': [
        '0-28 days with fever: ALWAYS full sepsis workup + empiric antibiotics + admission — no exceptions',
        '29-60 days: use PECARN rule (UA, ANC, procalcitonin) to identify low-risk infants who may not need LP',
        'HSV: acyclovir 20 mg/kg IV q8h for any neonate ≤21 days with fever + seizures, vesicles, or ill appearance',
        'Never delay antibiotics for LP — treat first if the infant is ill',
        'Outpatient management of low-risk 29-60 day olds requires IM ceftriaxone + 24-hour follow-up',
    ],
    'references': [
        'Kuppermann N, et al. A clinical prediction rule to identify febrile infants 60 days and younger at low risk for serious bacterial infections. JAMA Pediatr. 2019;173(4):342-351. (PECARN)',
        'Pantell RH, et al. Evaluation and management of well-appearing febrile infants 8 to 60 days old. Pediatrics. 2021;148(2):e2021052228. (AAP Clinical Practice Guideline)',
        'Gomez B, et al. Validation of the "Step-by-Step" approach in the management of young febrile infants. Pediatrics. 2016;138(2):e20154381.',
        'Kimberlin DW, et al. Guidance on management of asymptomatic neonates born to women with active genital herpes lesions. Pediatrics. 2013;131(2):e572-e579.',
        'Byington CL, et al. Serious bacterial infections in febrile infants 1 to 90 days old with and without viral infections. Pediatrics. 2004;113(6):1662-1666.',
    ],
}

if __name__ == '__main__':
    build_deck(spec, os.path.join(os.path.dirname(__file__), '19_febrile_infant.pptx'))

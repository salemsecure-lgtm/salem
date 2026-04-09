#!/usr/bin/env python3
"""Build Deck 30: PEM Board Review — High-Yield Pitfalls, Patterns, and Must-Not-Miss Diagnoses"""
import sys, os
sys.path.insert(0, os.path.dirname(__file__))
from build_deck import build_deck

spec = {
    'number': 30,
    'short_title': 'PEM Board Review',
    'title': 'PEM Board Review: High-Yield Pitfalls, Patterns, and Must-Not-Miss Diagnoses',
    'subtitle': 'The Ultimate High-Yield Review for PEM Board Preparation',
    'objectives': [
        'Review the most commonly tested PEM board topics and their classic presentations',
        'Identify board-style "red flags" and recognition patterns across all major topic areas',
        'Reinforce critical decision points that are frequently tested in PEM board exams',
        'Review key clinical pearls, landmark trials, and evidence-based guidelines',
        'Build confidence with rapid-fire clinical vignettes and pattern recognition',
    ],
    'slides': [
        {'type': 'content', 'title': 'Board Review Strategy: How This Deck Works',
         'bullets': [
             'This deck consolidates the highest-yield pearls from all 29 PEM topic decks',
             'Organized by: recognition patterns, critical actions, board traps, and key numbers',
             'Use this for final board preparation, rapid review before exams, and teaching',
             'Each slide = a high-yield board concept distilled to its essential teaching point',
             '>This is NOT a substitute for the full topic decks — it is a rapid-fire review',
         ],
         'notes': 'This deck is designed as the capstone review. Each slide distills a major PEM topic into its most testable, most critical teaching point. Use it for pre-board review, for morning conference rapid-fire review, or for self-testing. The full clinical detail is in the individual topic decks.'},

        {'type': 'section', 'title': 'Must-Not-Miss\nDiagnoses', 'subtitle': 'Patterns that save lives when recognized early'},

        {'type': 'table', 'title': 'The Must-Not-Miss List: Diagnoses That Kill If Delayed',
         'headers': ['Diagnosis', 'Classic Clue', 'Critical Action'],
         'rows': [
             ['Meningitis', 'Fever + AMS + bulging fontanelle', 'Antibiotics before LP — do not delay'],
             ['HSV encephalitis', 'Neonate + seizures + vesicles', 'Acyclovir 20 mg/kg IV q8h immediately'],
             ['Ductal-dependent CHD', 'Cyanosis unresponsive to O2, day 2-14', 'PGE1 0.05 mcg/kg/min — do not wait for echo'],
             ['Malrotation with volvulus', 'Bilious vomiting in neonate', 'Call surgery before imaging'],
             ['Testicular torsion', 'Acute scrotal pain + high-riding testis', 'OR within 6 hours for salvage'],
             ['Abusive head trauma', 'Unexplained AMS in infant', 'CT head + skeletal survey + ophthalmology'],
             ['Anaphylaxis', 'Multi-organ after allergen exposure', 'IM epinephrine 0.01 mg/kg FIRST'],
             ['Status epilepticus', 'Seizure >5 minutes', 'IM midazolam 0.2 mg/kg immediately'],
         ],
         'notes': 'This table should be memorized. Each row represents a diagnosis where delayed recognition or treatment leads to death or permanent disability. The "Critical Action" column is the one thing you must do immediately. Every PEM fellow should be able to execute these actions without hesitation.'},

        {'type': 'table', 'title': 'Key Numbers Every PEM Fellow Must Know',
         'headers': ['Topic', 'Key Number', 'Significance'],
         'rows': [
             ['Epinephrine IM', '0.01 mg/kg (max 0.3 child)', 'Anaphylaxis first-line dose'],
             ['Status epilepticus', '5 minutes', 'Definition threshold — start benzos'],
             ['Midazolam IM dose', '0.2 mg/kg (max 10 mg)', 'First-line for seizures without IV'],
             ['Dexamethasone for croup', '0.6 mg/kg (max 16 mg)', 'Single dose; all severity levels'],
             ['Magnesium for asthma', '50 mg/kg (max 2g)', 'IV over 20 min for severe asthma'],
             ['DKA insulin', '0.05-0.1 units/kg/hr', 'NO bolus — cerebral edema risk'],
             ['Hypertonic saline (3%)', '3-5 mL/kg IV', 'Cerebral edema in DKA, herniation, symptomatic hyponatremia'],
             ['PGE1', '0.05-0.1 mcg/kg/min', 'Ductal-dependent cardiac disease'],
             ['NAC loading dose', '150 mg/kg IV over 1h', 'Acetaminophen overdose'],
             ['Ketamine IV sedation', '1-2 mg/kg', 'PEM workhorse sedation agent'],
         ],
         'notes': 'These numbers are board favorites. They come up repeatedly in multiple-choice questions. Knowing these doses cold allows you to answer questions quickly and confidently. Notice the pattern: for each major emergency, there is ONE critical drug at ONE critical dose that you must know.'},

        {'type': 'content', 'title': 'Board Trap #1: Things That Are NOT What They Seem',
         'bullets': [
             'Apparent seizure during syncope → NOT epilepsy — it is hypoxic convulsive activity from cardiac arrest',
             'Quiet chest in asthma → NOT improvement — it means NO air movement (worse)',
             'Normal glucose after sulfonylurea ingestion → NOT safe — hypoglycemia can be delayed 8+ hours',
             'Well-appearing febrile neonate → NOT low risk — neonates can have occult SBI despite appearing well',
             'Negative FAST in pediatric trauma → NOT ruling out injury — sensitivity is only 50-75%',
             'Resolved wheezing after epinephrine → NOT cured croup — observe 2+ hours for rebound',
             'Normal CXR → NOT ruling out pneumonia — clinical diagnosis in well-appearing children',
         ],
         'notes': 'These "board traps" appear frequently in exam questions. The question stem will present a scenario that looks reassuring, and the correct answer requires you to recognize that the apparent good news is actually a trap. Each of these traps has a high-yield teaching point that can change your management.'},

        {'type': 'content', 'title': 'Board Trap #2: Things You Should NEVER Do',
         'bullets': [
             'NEVER give insulin bolus in DKA → increases cerebral edema risk',
             'NEVER give IV bicarbonate in pediatric DKA → increases cerebral edema risk',
             'NEVER examine the throat in suspected epiglottitis → can precipitate complete obstruction',
             'NEVER give codeine in children → FDA boxed warning for ultra-rapid metabolizer deaths',
             'NEVER give flumazenil with TCA co-ingestion → removes benzo protection against seizures',
             'NEVER give adenosine for wide complex tachycardia → can degenerate to VF',
             'NEVER discharge febrile neonate ≤28 days without full sepsis workup and admission',
             'NEVER skip epinephrine and only give antihistamines in anaphylaxis',
         ],
         'notes': 'These "never do" items are the most commonly tested negative knowledge items on boards. Each represents an action that, if taken, causes harm. They are designed to test whether you know the contraindications and pitfalls, not just the treatments. If you see these scenarios on the exam, the correct answer is always to NOT do the listed action.'},

        {'type': 'table', 'title': 'Landmark Trials and Guidelines to Know',
         'headers': ['Study/Guideline', 'Topic', 'Key Finding'],
         'rows': [
             ['PECARN Head CT Rule (Kuppermann 2009)', 'Pediatric head trauma', 'Identifies children at very low risk for ciTBI — safely reduces CT use'],
             ['RAMPART Trial (Silbergleit 2012)', 'Status epilepticus', 'IM midazolam ≥ IV lorazepam for prehospital seizures'],
             ['ESETT Trial (Kapur 2019)', 'Second-line anticonvulsants', 'Levetiracetam = fosphenytoin = valproate for benzodiazepine-refractory SE'],
             ['PECARN FLUID Trial (Kuppermann 2018)', 'DKA fluids', 'Fluid rate and type do NOT affect neurologic outcomes in DKA'],
             ['PECARN Febrile Infant (Kuppermann 2019)', 'Febrile infant risk stratification', 'UA + ANC + procalcitonin identifies low-risk 29-60 day olds'],
             ['AAP Maintenance Fluids (2018)', 'IV fluid selection', 'Isotonic (D5NS) for maintenance — hypotonic fluids cause iatrogenic hyponatremia'],
             ['CRASH-2', 'TXA in trauma', 'TXA within 3h reduces mortality in hemorrhagic trauma'],
         ],
         'notes': 'These trials and guidelines drive current PEM practice and are frequently referenced in board questions. Knowing the study name, population, and key finding is sufficient for boards. The PECARN studies (head CT, FLUID, febrile infant) are the most PEM-specific and most commonly tested.'},

        {'type': 'content', 'title': 'Rapid-Fire: Classic Board Vignettes',
         'bullets': [
             '2-month-old + rectal temp 38.5°C + well-appearing → full sepsis workup; may defer LP if PECARN low-risk',
             '18-month-old + barky cough + stridor at rest → dexamethasone 0.6 mg/kg + nebulized epinephrine',
             '3-year-old + intermittent abdominal pain + lethargy + "currant jelly" stool → intussusception → US → air enema',
             '5-year-old + peanut exposure + hives + wheezing + hypotension → epinephrine IM 0.01 mg/kg FIRST',
             '7-year-old + DKA + headache + vomiting 6h into treatment → cerebral edema → 3% NaCl 5 mL/kg STAT',
             '10-day-old + gray + mottled + SpO2 75% + weak femorals → ductal-dependent CHD → PGE1 immediately',
             '14-year-old + collapse during basketball + no prodrome → cardiac syncope → EKG → check for long QT/HCM',
             '4-month-old + unexplained bruise on cheek → NAT until proven otherwise → full workup + CPS report',
         ],
         'notes': 'These 8 vignettes represent the 8 most classic PEM board scenarios. Each can be answered in under 10 seconds if you know the pattern. Practice reading the vignette and immediately jumping to the diagnosis and the critical action. This is what board prep is about: pattern recognition and immediate correct response.'},

        {'type': 'two_column', 'title': 'Final Review: What Separates the Excellent PEM Fellow',
         'left_title': 'CLINICAL EXCELLENCE',
         'left': [
             'Recognizes sick vs. not sick immediately',
             'Starts treatment while workup is pending',
             'Gives analgesia early and reassesses',
             'Communicates clearly with families and team',
             'Knows when to call for help and does so early',
             'Documents thoroughly and objectively',
             'Maintains a broad differential until focused',
         ],
         'right_title': 'BOARD EXCELLENCE',
         'right': [
             'Knows the classic presentations and the atypical ones',
             'Identifies "board traps" and does not fall for them',
             'Knows key numbers and doses without looking them up',
             'Understands the evidence behind the guidelines',
             'Can apply prediction rules (PECARN, BMS, PAS) correctly',
             'Recognizes "what to do FIRST" in every scenario',
             'Thinks in algorithms, not just lists of facts',
         ],
         'notes': 'This final slide is about the mindset. PEM board excellence and clinical excellence are the same thing: rapid pattern recognition, decisive action, and a deep understanding of the "why" behind every guideline. Use this deck library not just to memorize facts, but to build the clinical reasoning framework that makes you an excellent PEM physician.'},

        {'type': 'pitfalls', 'title': 'The Top 10 PEM Board Pitfalls',
         'items': [
             '1. Delayed epinephrine in anaphylaxis (giving antihistamines first)',
             '2. Delayed antibiotics in suspected meningitis (waiting for LP)',
             '3. Insulin bolus in DKA (increases cerebral edema risk)',
             '4. CT-ing every child with head trauma (use PECARN)',
             '5. Missing NAT in infants with unexplained injuries',
             '6. Calling exercise-associated syncope "vasovagal"',
             '7. Not giving dexamethasone for mild croup',
             '8. Discharging after sulfonylurea ingestion with normal glucose',
             '9. Using hypotonic maintenance fluids',
             '10. Delaying PGE1 for suspected ductal-dependent CHD',
         ],
         'notes': 'These 10 pitfalls represent the 10 most commonly missed questions on PEM boards. Review them the night before your exam. If you can avoid all 10 of these errors, you are well-prepared. Each one has been covered in depth in the individual topic decks.'},
    ],
    'takeaways': [
        'Know the "must-not-miss" diagnoses and their critical actions — meningitis, HSV, ductal-dependent CHD, volvulus, torsion, NAT',
        'Know the key numbers: epinephrine 0.01 mg/kg, midazolam 0.2 mg/kg, dexamethasone 0.6 mg/kg, PGE1 0.05 mcg/kg/min',
        'Know the board traps: things that look reassuring but are dangerous (quiet chest, normal glucose after sulfonylurea)',
        'Know what NEVER to do: insulin bolus in DKA, examine throat in epiglottitis, codeine in children',
        'Think in patterns and algorithms — the best PEM physicians recognize, act, then investigate',
    ],
    'references': [
        'Kuppermann N, et al. PECARN head CT rule. Lancet. 2009;374:1160-1170.',
        'Silbergleit R, et al. RAMPART trial. N Engl J Med. 2012;366:591-600.',
        'Kapur J, et al. ESETT trial. N Engl J Med. 2019;381:2103-2113.',
        'Kuppermann N, et al. PECARN FLUID trial. N Engl J Med. 2018;378:2275-2287.',
        'Kuppermann N, et al. PECARN febrile infant rule. JAMA Pediatr. 2019;173:342-351.',
        'Shaker MS, et al. Anaphylaxis practice parameter update. JACI. 2020;145:1082-1123.',
        'AAP. Maintenance intravenous fluids in children. Pediatrics. 2018;142:e20183083.',
        'Wolfsdorf JI, et al. ISPAD DKA guidelines. Pediatr Diabetes. 2018;19(Suppl 27):155-177.',
    ],
}

if __name__ == '__main__':
    build_deck(spec, os.path.join(os.path.dirname(__file__), '30_pem_board_review.pptx'))

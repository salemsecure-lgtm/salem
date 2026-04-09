#!/usr/bin/env python3
"""Build Deck 13: Altered Mental Status in Children"""
import sys, os
sys.path.insert(0, os.path.dirname(__file__))
from build_deck import build_deck

spec = {
    'number': 13,
    'short_title': 'Altered Mental Status',
    'title': 'Altered Mental Status in Children',
    'subtitle': 'Systematic Approach to the Confused, Obtunded, or Unresponsive Child in the ED',
    'objectives': [
        'Build a systematic differential for altered mental status organized by age and acuity',
        'Apply the AEIOU-TIPS mnemonic to avoid missing life-threatening causes',
        'Prioritize the initial assessment and stabilization of the altered child',
        'Identify the key historical and exam features that narrow the differential',
        'Determine the appropriate workup and disposition for AMS in children',
    ],
    'slides': [
        {'type': 'content', 'title': 'Altered Mental Status in PEM: A Symptom, Not a Diagnosis',
         'bullets': [
             'AMS = any deviation from a child\'s baseline level of alertness, cognition, or behavior',
             'Ranges from irritability and lethargy to obtundation and coma',
             'Critically important: parents know their child best — take "not acting right" seriously',
             'AMS in children has a BROAD differential — the age of the child helps narrow it',
             'Initial approach: stabilize, rapid glucose, rapid assessment, then systematic workup',
             '>The biggest risk: assuming altered mental status is "just post-ictal" or "just viral" without ruling out treatable emergencies',
         ],
         'notes': 'Open by emphasizing that AMS is a chief complaint that demands respect. In children, the presentation can be subtle — a lethargic infant may just seem "sleepy" to a triage nurse. Parents who say their child is "not acting normal" are usually right. The PEM physician must be the person who takes this complaint seriously and works through the differential systematically.'},

        {'type': 'table', 'title': 'AEIOU-TIPS: Differential Diagnosis Framework',
         'headers': ['Letter', 'Category', 'Examples'],
         'rows': [
             ['A', 'Alcohol / Abuse', 'Ingestion, NAT (shaken baby, abusive head trauma)'],
             ['E', 'Endocrine / Electrolyte / Encephalopathy', 'DKA, hypoglycemia, hypo/hypernatremia, uremia, hepatic failure'],
             ['I', 'Insulin (Hypoglycemia)', 'Most common reversible cause — check glucose IMMEDIATELY'],
             ['O', 'Oxygen / Overdose', 'Hypoxia, carbon monoxide, toxic ingestion'],
             ['U', 'Uremia', 'HUS, renal failure, uremic encephalopathy'],
             ['T', 'Trauma / Temperature', 'Head injury, NAT, hyperthermia, hypothermia'],
             ['I', 'Infection', 'Meningitis, encephalitis, sepsis, brain abscess'],
             ['P', 'Psychiatric / Poisoning', 'Conversion disorder (diagnosis of exclusion), ingestion'],
             ['S', 'Seizure / Stroke / Shunt / Space-occupying lesion', 'Post-ictal, stroke (sickle cell), VP shunt malfunction, brain tumor'],
         ],
         'notes': 'AEIOU-TIPS is a comprehensive mnemonic for the differential of AMS in children. Emphasize that the I for Insulin (hypoglycemia) should be checked within the first 60 seconds — point-of-care glucose. The mnemonic helps you avoid anchoring on one diagnosis. Work through it systematically.'},

        {'type': 'algorithm', 'title': 'First 10 Minutes: Rapid Assessment of AMS',
         'steps': [
             'ABCs: secure airway if GCS ≤8, check breathing, check perfusion',
             'Point-of-care glucose: treat hypoglycemia immediately (D10W 5 mL/kg IV)',
             'Vital signs + SpO2: fever? hypotension? bradycardia? hypertension?',
             'Rapid neuro exam: pupils, GCS/AVPU, fontanelle (if open), focal deficits, posturing',
             'Brief history from parents: timeline, preceding events, medications in the home, trauma',
             'Consider empiric antibiotics if sepsis/meningitis suspected (do not delay for LP)',
             'Consider naloxone if opioid ingestion suspected (0.1 mg/kg IV, max 2 mg)',
         ],
         'notes': 'This is your protocol for the first 10 minutes. The order matters: airway first, glucose second, everything else third. A GCS ≤8 means the airway is at risk and intubation should be considered. Fontanelle assessment is critical in infants — a bulging fontanelle suggests increased ICP (meningitis, hydrocephalus, NAT). Do not delay antibiotics for LP if you suspect meningitis.'},

        {'type': 'two_column', 'title': 'AMS by Age: Common Causes',
         'left_title': 'INFANTS (0-12 months)',
         'left': [
             'Non-accidental trauma (NAT) — #1 concern',
             'Meningitis / sepsis',
             'Inborn errors of metabolism',
             'Intussusception (lethargy as presenting sign)',
             'Hypoglycemia',
             'Electrolyte derangement',
             'Seizure (may be subtle)',
             'Congenital heart disease (low cardiac output)',
         ],
         'right_title': 'TODDLERS / CHILDREN',
         'right': [
             'Toxic ingestion (most common in 1-5 year olds)',
             'Post-ictal state',
             'Meningitis / encephalitis',
             'DKA (new-onset diabetes)',
             'Trauma (accidental or NAT)',
             'Intracranial mass / hydrocephalus',
             'VP shunt malfunction (if present)',
             'Stroke (sickle cell, cardiac disease)',
         ],
         'notes': 'Age-based thinking is essential. In infants, NAT must ALWAYS be on your differential — a lethargic infant with no clear explanation is concerning for abusive head trauma until proven otherwise. In toddlers, toxic ingestion is the most common cause of acute AMS. In school-age children, consider DKA presenting with new-onset diabetes.'},

        {'type': 'content', 'title': 'Focused History: Critical Questions',
         'bullets': [
             'Timeline: sudden vs. gradual onset? (sudden = vascular, seizure, trauma; gradual = metabolic, infectious)',
             'Preceding symptoms: fever? headache? vomiting? seizure witnessed?',
             'Medications and substances: what is in the home? Access to grandparents\' meds? Recent medication changes?',
             'Trauma: any fall, injury, or mechanism? (ask specifically about NAT if infant/toddler)',
             'Medical history: seizure disorder? VP shunt? diabetes? sickle cell? cardiac disease?',
             'Recent illness: URI? gastroenteritis? (consider post-infectious encephalitis, ADEM)',
             'Family history: metabolic disease? seizures? consanguinity?',
             '>If the history doesn\'t match the presentation, think NAT or unreported ingestion',
         ],
         'notes': 'History is your most powerful diagnostic tool in AMS. The timeline is critical — sudden onset points toward vascular, traumatic, or seizure etiologies. Gradual onset suggests metabolic or infectious causes. Always ask about medications in the home — toddlers are mobile and curious. And always consider NAT when the history does not explain the severity of presentation.'},

        {'type': 'content', 'title': 'Key Exam Findings and What They Suggest',
         'bullets': [
             'Bulging fontanelle → increased ICP: meningitis, NAT, hydrocephalus',
             'Retinal hemorrhages → NAT (abusive head trauma) — highly specific',
             'Unilateral dilated pupil → uncal herniation: mass, hemorrhage, herniation',
             'Bilateral pinpoint pupils → opioid ingestion',
             'Bilateral dilated pupils → anticholinergic toxidrome',
             'Fever + AMS → meningitis/encephalitis until proven otherwise',
             'Hypertension + bradycardia → Cushing response (increased ICP)',
             'Fruity breath + Kussmaul respirations → DKA',
             'Track marks, medication patches, unusual odor → toxic ingestion',
         ],
         'notes': 'The physical exam in AMS is targeted and efficient. Pupils are incredibly informative — asymmetric pupils in an obtunded child is a neurosurgical emergency (uncal herniation). Retinal hemorrhages with AMS in an infant = abusive head trauma until proven otherwise. The combination of fever + altered mental status demands meningitis coverage before any other workup.'},

        {'type': 'table', 'title': 'Initial Workup for AMS',
         'headers': ['Test', 'When to Order', 'What It Rules In/Out'],
         'rows': [
             ['Point-of-care glucose', 'ALWAYS — first test', 'Hypoglycemia (most common treatable cause)'],
             ['BMP (electrolytes)', 'Always', 'Na, K, Ca, glucose, BUN/Cr, anion gap'],
             ['VBG/ABG', 'Ill-appearing or respiratory distress', 'Acidosis (DKA, sepsis, metabolic), CO2 level'],
             ['CBC', 'Fever or infectious concern', 'WBC, anemia, thrombocytopenia (HUS, sepsis)'],
             ['Blood culture', 'Fever or sepsis concern', 'Bacteremia'],
             ['UA + urine tox screen', 'Ingestion suspected', 'Drug screen (limited sensitivity), UTI'],
             ['Ammonia + lactate', 'Infant or suspected metabolic disorder', 'Inborn errors, mitochondrial disease'],
             ['CT head (non-contrast)', 'Trauma, focal deficit, papilledema, GCS ≤12', 'ICH, mass, hydrocephalus, NAT'],
             ['LP', 'After CT (if safe), if meningitis/encephalitis suspected', 'CSF pleocytosis, gram stain, culture, HSV PCR'],
         ],
         'notes': 'The workup is guided by the clinical picture. Glucose is always first. If the child is febrile and altered, get blood cultures and start empiric antibiotics (ceftriaxone + vancomycin ± acyclovir if HSV concern) before LP. CT head before LP if there is concern for increased ICP. Ammonia and lactate are critical in any infant with unexplained AMS — inborn errors of metabolism present this way.'},

        {'type': 'case', 'title': '9-Month-Old Found "Sleepy" by Mother',
         'scenario': 'A 9-month-old previously healthy girl is brought by her mother who says the baby has been "too sleepy" since waking from her nap 2 hours ago. She is not feeding well and is not interacting normally. No fever, no vomiting, no trauma per mother. The boyfriend was babysitting while mother was at work. On exam: lethargic, intermittently moaning, anterior fontanelle is full and tense, GCS 10 (E2V3M5). Pupils equal and reactive.',
         'questions': [
             'What is your top concern? (Non-accidental trauma — abusive head trauma)',
             'Why? (Unexplained AMS in infant + bulging fontanelle + caregiver was boyfriend + no history of trauma)',
             'Immediate workup? (CT head STAT, skeletal survey, ophthalmology for dilated fundoscopic exam)',
             'CT shows bilateral subdural hematomas — next steps? (Neurosurgery, child protection team, admit to PICU)',
         ],
         'notes': 'This case must be taught sensitively but directly. Abusive head trauma is the leading cause of traumatic death in infants. The classic presentation: an infant with unexplained altered mental status, bulging fontanelle, and a caregiver who gives no history of trauma. The triad of subdural hematomas, retinal hemorrhages, and encephalopathy is highly specific for NAT. You MUST consider this diagnosis.'},

        {'type': 'pitfalls', 'title': 'AMS Pitfalls in PEM',
         'items': [
             'Forgetting to check glucose — the simplest and most treatable cause of AMS',
             'Attributing altered mental status to a "post-ictal state" without considering WHY the seizure happened',
             'Not considering NAT in infants with unexplained AMS — ask direct questions, examine carefully',
             'Delaying antibiotics in febrile AMS while waiting for LP results — treat first, diagnose second',
             'Forgetting toxic ingestion in toddlers — "pill in the pocket" can be fatal (calcium channel blockers, opioids, sulfonylureas)',
             'Not checking ammonia in neonates/young infants with AMS — inborn errors of metabolism present acutely',
             'Assuming "viral illness" in a lethargic infant — lethargy in infants is ALWAYS a red flag',
         ],
         'notes': 'The unifying theme: AMS in children requires a systematic approach. Do not anchor on one diagnosis. Do not dismiss a lethargic infant as "viral" without a thorough workup. Every pitfall on this list has led to patient harm in real clinical practice.'},
    ],
    'takeaways': [
        'Check point-of-care glucose within 60 seconds of recognizing AMS — hypoglycemia is the #1 treatable cause',
        'Use AEIOU-TIPS to systematically work through the differential and avoid anchoring',
        'Unexplained AMS in an infant = non-accidental trauma until proven otherwise',
        'Febrile + altered = start empiric meningitis coverage BEFORE LP (ceftriaxone + vancomycin ± acyclovir)',
        'Toxic ingestion is the most common cause of acute AMS in toddlers — search the house, check a tox screen',
    ],
    'references': [
        'Avner JR. Altered states of consciousness. Pediatr Rev. 2006;27(9):331-338.',
        'Mistry RD, et al. Clinical outcomes of emergency department patients with altered mental status. Acad Emerg Med. 2016;23(11):1326-1335.',
        'Kuppermann N, et al. Identification of children at very low risk of clinically-important brain injuries after head trauma: a prospective cohort study. Lancet. 2009;374(9696):1160-1170.',
        'Christian CW, et al. Abusive head trauma in infants and children. Pediatrics. 2009;123(5):1409-1411.',
        'Nigrovic LE, et al. The Yale Observation Scale score and the risk of serious bacterial illness in febrile infants. Pediatrics. 2017;140(1):e20170695.',
        'Barsan WG. Altered mental status and coma. In: Tintinalli JE, ed. Emergency Medicine. 9th ed. McGraw-Hill; 2020.',
    ],
}

if __name__ == '__main__':
    build_deck(spec, os.path.join(os.path.dirname(__file__), '13_altered_mental_status.pptx'))

#!/usr/bin/env python3
"""Build Deck 9: Pneumonia and Complicated Pneumonia in the ED"""
import sys, os
sys.path.insert(0, os.path.dirname(__file__))
from build_deck import build_deck

spec = {
    'number': 9,
    'short_title': 'Pneumonia in the ED',
    'title': 'Pneumonia and Complicated Pneumonia in the ED',
    'subtitle': 'Evidence-Based Diagnosis, Antibiotic Selection, and Red Flag Recognition',
    'objectives': [
        'Identify when imaging is and is NOT indicated for suspected pneumonia',
        'Select appropriate empiric antibiotics by age group and severity',
        'Recognize complicated pneumonia: empyema, necrotizing pneumonia, lung abscess',
        'Apply evidence-based disposition criteria for pediatric CAP',
        'Identify the child with pneumonia who needs escalation vs. outpatient management',
    ],
    'slides': [
        {'type': 'content', 'title': 'Community-Acquired Pneumonia in Children: PEM Framework',
         'bullets': [
             'Leading infectious cause of death in children worldwide',
             'CAP responsible for ~1-4% of pediatric ED visits in developed countries',
             'Most pneumonia in children <5 years is VIRAL — antibiotics often unnecessary',
             'Bacterial CAP increases with age; S. pneumoniae remains the most common bacterial cause at all ages',
             'Atypical pathogens (Mycoplasma) become important after age 5',
             'The PEM challenge: identify who needs antibiotics, imaging, and admission vs. who goes home',
             '>Not every child with fever and cough has pneumonia. Not every pneumonia needs antibiotics.',
         ],
         'notes': 'Frame the talk around the core PEM dilemma: overcalling pneumonia leads to unnecessary antibiotics and imaging, but undercalling it can miss serious bacterial disease. The majority of lower respiratory infections in young children are viral. The goal is to identify the subset who truly have bacterial pneumonia and need treatment.'},

        {'type': 'table', 'title': 'Pathogens by Age Group',
         'headers': ['Age Group', 'Most Common Pathogens', 'Empiric Antibiotic'],
         'rows': [
             ['Neonate (<30d)', 'GBS, E. coli, Listeria, HSV', 'Ampicillin + gentamicin (+ acyclovir if HSV concern)'],
             ['1-3 months', 'Chlamydia trachomatis, RSV, Pertussis, S. pneumo', 'Macrolide (afebrile) or ampicillin (febrile)'],
             ['3 months - 5 years', 'Viral (RSV, rhinovirus, influenza), S. pneumoniae', 'High-dose amoxicillin 90 mg/kg/day (outpatient)'],
             ['5-18 years', 'Mycoplasma, S. pneumoniae, Chlamydophila pneumo', 'Amoxicillin + macrolide OR macrolide alone'],
             ['Any age, severe', 'S. pneumoniae, S. aureus, S. pyogenes', 'IV ampicillin or ceftriaxone ± vancomycin'],
         ],
         'notes': 'This table is the clinical backbone of the talk. High-dose amoxicillin (90 mg/kg/day) is the standard outpatient treatment for uncomplicated CAP in children — this is the IDSA/PIDS recommendation. For the neonate, the approach is entirely different and follows neonatal sepsis protocols. For school-age children, atypical coverage with a macrolide is important.'},

        {'type': 'two_column', 'title': 'When to Image — and When NOT to Image',
         'left_title': 'DO GET CHEST X-RAY',
         'left': [
             'Hypoxia (SpO2 <92%)',
             'Significant respiratory distress',
             'Failed outpatient antibiotics',
             'Concern for complicated pneumonia (effusion, empyema)',
             'Ill-appearing or toxic child',
             'ICU-level care anticipated',
             'Recurrent pneumonia (evaluate for anatomic cause)',
         ],
         'right_title': 'DO NOT NEED CXR',
         'right': [
             'Well-appearing child with clinical pneumonia (focal findings)',
             'Uncomplicated presentation planned for outpatient antibiotics',
             'Wheezing-predominant illness (more likely viral/reactive)',
             'Simple URI with fever and no focal findings',
             'Routine follow-up CXR for uncomplicated CAP (not recommended)',
             'Bronchiolitis presentation in age-appropriate infant',
         ],
         'notes': 'The PIDS/IDSA guidelines are clear: routine CXR is NOT necessary for well-appearing children with uncomplicated CAP who will be treated as outpatients. This is evidence-based and reduces unnecessary radiation and ED time. However, if the child is ill-appearing, hypoxic, or you suspect complicated pneumonia, imaging is essential.'},

        {'type': 'content', 'title': 'Red Flags: When Pneumonia Is Complicated',
         'bullets': [
             'Parapneumonic effusion: found in 40% of hospitalized CAP; most are small and self-resolving',
             'Empyema: infected pleural fluid — fever persisting >48-72h despite appropriate antibiotics',
             'Necrotizing pneumonia: tissue destruction, often S. aureus or S. pneumoniae — CT finding',
             'Lung abscess: cavitary lesion — often polymicrobial, associated with aspiration',
             'Clinical clues to complicated disease:',
             '>Persistent/recurrent fever despite 48-72h of appropriate antibiotics',
             '>Large pleural effusion on CXR (meniscus sign, opacification of hemithorax)',
             '>Chest pain, splinting, decreased breath sounds with dullness to percussion',
             '>Toxic appearance out of proportion to what you expect',
         ],
         'notes': 'The key clinical decision point: when a child with pneumonia is not improving after 48-72 hours of appropriate antibiotics, you must consider complicated pneumonia. Get a chest ultrasound (preferred) or CT to evaluate for effusion, empyema, or necrotizing pneumonia. Ultrasound is increasingly first-line for pleural effusion assessment.'},

        {'type': 'algorithm', 'title': 'Parapneumonic Effusion Management Algorithm',
         'steps': [
             'Small effusion (<10mm on lateral decubitus or US): Continue antibiotics, observe',
             'Moderate effusion (>10mm, no loculations): Consider thoracentesis — send fluid for culture, cell count, pH, glucose, LDH, gram stain',
             'Large/loculated effusion (empyema): Chest tube + fibrinolytics (tPA + DNase) OR VATS',
             'Empyema criteria: pH <7.2, glucose <40, LDH >1000, pus on aspiration, positive culture',
             'Fibrinolytics protocol: tPA 4 mg + DNase 5 mg in 30-40 mL NS via chest tube, clamp 1 hr, drain',
             'VATS if: failed fibrinolytics, organized empyema, multiloculated on CT',
         ],
         'notes': 'This algorithm covers a common PEM/surgical decision. The trend has been toward earlier intervention with fibrinolytics via chest tube, which has reduced the need for VATS in many centers. The choice between chest tube + fibrinolytics vs. VATS is institution-dependent. Know your local practice. The PEM role: recognize complicated effusion early and involve surgery.'},

        {'type': 'content', 'title': 'Pneumonia in Special Populations',
         'bullets': [
             'Immunocompromised children: broader differential (PJP, fungal, CMV, TB) — early infectious disease consult',
             'Sickle cell disease: acute chest syndrome mimics pneumonia — treat with antibiotics + transfusion if needed',
             'Aspiration pneumonia: recurrent RLL/RML pneumonia — consider swallow study, H-type TEF, neuromuscular disorder',
             'Round pneumonia: well-defined round opacity in a child <8 years — mimics mass on CXR, treat as pneumonia',
             'Neonatal pneumonia: part of neonatal sepsis workup — GBS prophylaxis history is critical',
             '>When in doubt about the diagnosis, ask: could this be something else entirely?',
         ],
         'notes': 'Round pneumonia is a board favorite — it occurs because of incomplete development of pores of Kohn and channels of Lambert, so infection cannot spread between segments and forms a round consolidation. It mimics a mass. In a child <8 with fever and a round opacity, treat as pneumonia first. If it does not improve, then consider further workup.'},

        {'type': 'table', 'title': 'Antibiotic Dosing Quick Reference',
         'headers': ['Drug', 'Route', 'Dose', 'Frequency', 'Key Notes'],
         'rows': [
             ['Amoxicillin', 'PO', '90 mg/kg/day', 'Divided BID or TID', 'First-line outpatient CAP'],
             ['Amox-clav', 'PO', '90 mg/kg/day (amox)', 'BID (14:1 formulation)', 'If concern for resistant organism'],
             ['Azithromycin', 'PO', '10 mg/kg day 1, then 5 mg/kg', 'Daily x 5 days', 'Atypical coverage; age >5'],
             ['Ampicillin', 'IV', '200 mg/kg/day', 'Divided q6h', 'First-line inpatient uncomplicated'],
             ['Ceftriaxone', 'IV/IM', '50-100 mg/kg/day', 'Daily or BID', 'Inpatient, or single IM dose for ED discharge'],
             ['Clindamycin', 'IV/PO', '40 mg/kg/day', 'Divided q6-8h', 'S. aureus coverage; empyema'],
             ['Vancomycin', 'IV', '60 mg/kg/day', 'Divided q6h', 'MRSA concern; severe/ICU-level'],
         ],
         'notes': 'Keep this as a quick reference. The IDSA/PIDS guidelines emphasize high-dose amoxicillin as first-line. IV ampicillin is preferred over ceftriaxone for uncomplicated inpatient CAP because it has better S. pneumoniae coverage and narrower spectrum. Ceftriaxone is used when broader coverage is needed or for IM dosing in the ED.'},

        {'type': 'two_column', 'title': 'Admit vs. Discharge',
         'left_title': 'ADMIT CRITERIA',
         'left': [
             'Hypoxia (SpO2 <92% on room air)',
             'Significant respiratory distress / work of breathing',
             'Inability to tolerate oral fluids or medications',
             'Failed outpatient treatment',
             'Toxic appearance or sepsis concern',
             'Complicated pneumonia (effusion/empyema)',
             'Age <3-6 months with bacterial pneumonia',
             'Underlying comorbidities (immunocompromised, cardiac, etc.)',
             'Social concerns (unreliable follow-up, transport issues)',
         ],
         'right_title': 'SAFE FOR DISCHARGE',
         'right': [
             'SpO2 ≥92% on room air',
             'Mild-moderate symptoms, comfortable',
             'Tolerating PO fluids and oral antibiotics',
             'No significant respiratory distress',
             'Adequate hydration',
             'Reliable caregiver and follow-up',
             'No complicated pneumonia findings',
             'Appropriate age and no comorbidities',
         ],
         'notes': 'The SpO2 threshold of 92% is the standard admission cutoff per guidelines. Some institutions use 90%. The key teaching point: the decision is not just about the oxygen level — it is about the overall clinical picture. A well-appearing child with SpO2 of 91% who is drinking and playful may be safe to observe; a toxic child with SpO2 of 93% may need admission.'},

        {'type': 'case', 'title': '3-Year-Old with Persistent Fever Despite Antibiotics',
         'scenario': 'A 3-year-old boy presents on day 4 of amoxicillin for pneumonia diagnosed at an urgent care. Parents report persistent high fevers (39.5°C daily), decreased oral intake, and worsening cough. He appears ill, is breathing fast (RR 42), and has decreased breath sounds on the right with dullness to percussion. SpO2 is 93% on room air.',
         'questions': [
             'What is your concern? (Complicated pneumonia — parapneumonic effusion vs. empyema)',
             'Best initial imaging? (Bedside chest ultrasound — can identify and characterize effusion)',
             'US shows large, complex effusion with septations — next step?',
             'Involve surgery: chest tube with fibrinolytics vs. VATS; IV antibiotics (ceftriaxone + clindamycin)',
         ],
         'notes': 'This case illustrates the classic complicated pneumonia scenario. The key: fever persisting >48-72h on appropriate antibiotics = you must reassess. Dullness to percussion + decreased breath sounds = effusion until proven otherwise. Bedside US is your best friend here — it is more sensitive than CXR for detecting and characterizing effusions.'},

        {'type': 'pitfalls', 'title': 'Pneumonia Pitfalls in PEM',
         'items': [
             'Routine CXR for every child with cough and fever — most do not need imaging',
             'Treating viral lower respiratory infections with antibiotics',
             'Using ceftriaxone when ampicillin or amoxicillin would suffice (antibiotic stewardship)',
             'Not giving adequate amoxicillin dose (must be 90 mg/kg/day for pneumococcal coverage)',
             'Missing complicated pneumonia — fever >72h on antibiotics demands reassessment',
             'Forgetting atypical coverage in school-age children (Mycoplasma)',
             'Ordering routine follow-up CXR for uncomplicated CAP (not recommended by guidelines)',
         ],
         'notes': 'Antibiotic stewardship is a major theme. The biggest pitfall in PEM is overtreating viral illness with antibiotics. When antibiotics ARE indicated, use the right one at the right dose. And the most dangerous pitfall is missing complicated pneumonia by not reassessing the child who is not improving.'},
    ],
    'takeaways': [
        'Most pneumonia in children <5 years is viral — do not reflexively prescribe antibiotics',
        'High-dose amoxicillin (90 mg/kg/day) is first-line for outpatient bacterial CAP',
        'CXR is NOT routinely needed for well-appearing children with uncomplicated pneumonia',
        'Fever persisting >48-72h on appropriate antibiotics = complicated pneumonia until proven otherwise',
        'Bedside ultrasound is the best initial test for suspected parapneumonic effusion',
    ],
    'references': [
        'Bradley JS, et al. The management of community-acquired pneumonia in infants and children older than 3 months of age: clinical practice guidelines by the PIDS and IDSA. Clin Infect Dis. 2011;53(7):e25-76.',
        'Jain S, et al. Community-acquired pneumonia requiring hospitalization among U.S. children. N Engl J Med. 2015;372(9):835-845.',
        'Williams DJ, et al. Short- vs standard-course outpatient antibiotic therapy for community-acquired pneumonia in children: the SCOUT-CAP randomized clinical trial. JAMA Pediatr. 2022;176(3):253-261.',
        'Balfour-Lynn IM, et al. BTS guidelines for the management of pleural infection in children. Thorax. 2005;60(Suppl 1):i1-21.',
        'Shah SS, et al. Intrapleural fibrinolysis is effective in the treatment of complicated parapneumonic effusion. J Pediatr Surg. 2007;42(12):2062-2067.',
    ],
}

if __name__ == '__main__':
    build_deck(spec, os.path.join(os.path.dirname(__file__), '09_pneumonia_complicated.pptx'))

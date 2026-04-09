#!/usr/bin/env python3
"""Build Deck 28: PEM Ultrasound Essentials (POCUS)"""
import sys, os
sys.path.insert(0, os.path.dirname(__file__))
from build_deck import build_deck

spec = {
    'number': 28,
    'short_title': 'PEM POCUS',
    'title': 'PEM Ultrasound Essentials (POCUS for Fellows)',
    'subtitle': 'Core Applications, Image Acquisition, and Clinical Integration of Point-of-Care Ultrasound',
    'objectives': [
        'Perform and interpret the pediatric FAST exam for trauma',
        'Use POCUS for common PEM applications: pneumothorax, intussusception, appendicitis, fracture',
        'Integrate cardiac POCUS into resuscitation: pericardial effusion, contractility assessment',
        'Apply POCUS for procedures: IV access, abscess identification, foreign body localization',
        'Understand POCUS limitations and know when to defer to formal imaging',
    ],
    'slides': [
        {'type': 'content', 'title': 'POCUS in PEM: An Essential Fellow Skill',
         'bullets': [
             'POCUS is a bedside diagnostic tool — it extends the physical exam, not replaces formal imaging',
             'PEM POCUS applications are expanding: diagnostic, procedural, resuscitative, and monitoring',
             'ACGME now requires POCUS competency for PEM fellows',
             'Key principle: POCUS answers focused clinical questions — it is not a comprehensive exam',
             'Clinical integration: POCUS findings change management in 25-50% of PED cases where it is used',
             '>The best ultrasound is the one done at the bedside, in real time, by the treating physician',
         ],
         'notes': 'POCUS has transformed PEM practice. Fellows must be proficient in core applications. The key mindset: POCUS answers a specific question ("Is there free fluid? Is there a pericardial effusion? Is this an abscess?"). It does not replace comprehensive radiologist-performed US. Use it to make faster, better decisions at the bedside.'},

        {'type': 'table', 'title': 'Core PEM POCUS Applications',
         'headers': ['Application', 'Clinical Question', 'Probe', 'Key Finding'],
         'rows': [
             ['FAST (trauma)', 'Is there free fluid?', 'Curvilinear', 'Anechoic stripe in Morison, splenorenal, pelvis, pericardium'],
             ['Pneumothorax', 'Is there lung sliding?', 'Linear', 'Absent lung sliding, absent comet tails = PTX; barcode sign on M-mode'],
             ['Cardiac (Echo)', 'Pericardial effusion? Contractility?', 'Phased array', 'Anechoic space around heart; qualitative contractility'],
             ['Appendicitis', 'Can I see a dilated appendix?', 'Linear/curvilinear', 'Non-compressible, >6mm, target sign, hyperemia'],
             ['Intussusception', 'Is there a target/doughnut sign?', 'Curvilinear', 'Target sign in transverse, pseudokidney in longitudinal'],
             ['Abscess/cellulitis', 'Is there a drainable collection?', 'Linear', 'Anechoic/hypoechoic fluid collection with surrounding hyperemia'],
             ['Fracture', 'Is there a cortical disruption?', 'Linear', 'Step-off or disruption of hyperechoic cortex'],
             ['IV guidance', 'Where is the vein?', 'Linear', 'Compressible anechoic circle; real-time needle guidance'],
         ],
         'notes': 'This table covers the 8 core PEM POCUS applications. Each answers a focused question with a specific probe and key finding. The FAST exam, pneumothorax assessment, and cardiac echo are resuscitative applications. Appendicitis and intussusception US are diagnostic. Abscess ID and IV guidance are procedural. Fellows should aim for competency in all 8.'},

        {'type': 'content', 'title': 'Pediatric FAST Exam: Technique and Interpretation',
         'bullets': [
             '4 views: RUQ (Morison pouch), LUQ (splenorenal), suprapubic (pelvis), subxiphoid (pericardium)',
             'RUQ: hepatorenal interface — most sensitive location for free fluid in supine patient',
             'LUQ: splenorenal and diaphragmatic — tip the probe superiorly to see above the spleen',
             'Suprapubic: look for free fluid posterior to the bladder (pouch of Douglas in females)',
             'Subxiphoid: pericardial effusion — anechoic stripe between myocardium and pericardium',
             'FAST sensitivity in pediatric trauma: 50-75% (lower than adults due to smaller fluid volumes)',
             '>A NEGATIVE FAST does NOT rule out solid organ injury in children — sensitivity is limited',
             '>If clinical suspicion is high, proceed to CT regardless of FAST result',
         ],
         'notes': 'The key limitation of FAST in children: it has lower sensitivity than in adults. Small-volume free fluid can be missed, and solid organ injuries without significant free fluid will not be detected. FAST is most useful for: 1) unstable patient — FAST positive → OR; 2) low mechanism — FAST negative → observation may be appropriate. For intermediate-risk patients, CT is more definitive.'},

        {'type': 'content', 'title': 'Lung Ultrasound: Pneumothorax and Effusion',
         'bullets': [
             'Pneumothorax: absence of lung sliding at the pleural line — sensitivity >90%',
             'Normal: "seashore sign" on M-mode (lung sliding present)',
             'Pneumothorax: "barcode sign" (stratosphere sign) on M-mode (no lung sliding)',
             'Lung point: transition between sliding and non-sliding = specific for pneumothorax',
             'Pleural effusion: anechoic layer above the diaphragm; can guide thoracentesis',
             'B-lines (comet tails): >3 per rib space suggests pulmonary edema or fluid overload',
             'Consolidation: hepatization of lung tissue — used for pneumonia diagnosis',
             '>Lung US for pneumothorax is MORE sensitive than chest X-ray and available immediately',
         ],
         'notes': 'Lung ultrasound is one of the most impactful POCUS applications. For pneumothorax, it is more sensitive than CXR and can be done in seconds. This is critical in trauma (tension pneumothorax assessment) and in the mechanically ventilated patient. For pleural effusion, it guides management and thoracentesis. Lung US for pneumonia diagnosis is emerging as a radiation-free alternative to CXR.'},

        {'type': 'algorithm', 'title': 'POCUS in Pediatric Resuscitation',
         'steps': [
             'CARDIAC ARREST: subxiphoid view during pulse check — assess for tamponade, severe hypovolemia, RV dilation (PE)',
             'SHOCK: IVC assessment (collapsibility = need for fluids), cardiac contractility, pericardial effusion',
             'RESPIRATORY FAILURE: lung sliding (pneumothorax?), B-lines (edema?), consolidation (pneumonia?), effusion',
             'HYPOTENSION: focused cardiac echo — EF assessment, pericardial effusion, RV strain (PE)',
             'TRAUMA: FAST exam + extended lung views for pneumothorax (E-FAST)',
             'Do NOT let POCUS delay resuscitation — integrate it into existing workflow during natural pauses',
         ],
         'notes': 'POCUS during resuscitation should be integrated, not disruptive. In cardiac arrest, use the pulse check pause (every 2 minutes) to get a subxiphoid view — 10 seconds is enough. In shock, IVC assessment helps guide fluid resuscitation. In respiratory failure, lung US can rapidly differentiate pneumothorax from effusion from consolidation. The key: POCUS should CHANGE MANAGEMENT, not just confirm what you already know.'},

        {'type': 'content', 'title': 'Procedural POCUS: Game-Changing Applications',
         'bullets': [
             'Ultrasound-guided IV access: success rate >90% even in difficult access patients',
             'Use linear probe in short-axis (transverse) view; visualize needle entering vein in real-time',
             'Abscess vs. cellulitis: US distinguishes drainable collection from diffuse inflammation',
             'Abscess US: hypoechoic collection that may show posterior acoustic enhancement',
             'Foreign body detection: glass, metal, wood can be visualized as hyperechoic structures with shadowing',
             'Peritonsillar abscess: intraoral US can guide diagnosis and drainage',
             'Fracture identification: cortical disruption of long bones — useful for toddler fractures and distal radius',
             '>US-guided IV in a dehydrated child saves time, reduces pain, and increases first-attempt success',
         ],
         'notes': 'US-guided IV access is perhaps the most commonly used procedural POCUS application in PEM. In the dehydrated child or the obese child with no visible veins, it transforms care. The technique: place the linear probe perpendicular to the vein (short-axis view), center the vein on screen, and advance the needle while tracking the tip. Practice on phantoms and then on easy patients before attempting difficult access.'},

        {'type': 'case', 'title': '4-Year-Old After MVC with Abdominal Pain',
         'scenario': 'A 4-year-old restrained backseat passenger in a moderate-speed MVC. He is crying, complaining of abdominal pain. HR 130, BP 90/60, RR 28, SpO2 98%. Abdomen is mildly distended with generalized tenderness. While the team is establishing IV access, you perform a bedside FAST exam.',
         'questions': [
             'FAST RUQ: you see a thin anechoic stripe in Morison pouch — interpretation? (Free fluid — positive FAST)',
             'Does this mean he definitely needs surgery? (Not necessarily — it means intra-abdominal injury is likely; he needs CT)',
             'If he were hemodynamically unstable with a positive FAST? (Directly to OR — FAST positive + unstable = operative)',
             'He is borderline stable → CT with IV contrast → spleen laceration grade III → non-operative management with surgery and PICU admission',
         ],
         'notes': 'This case integrates POCUS into real-time trauma management. The FAST result changes the clinical trajectory: it confirms intra-abdominal injury and helps triage (OR vs. CT). The distinction: FAST positive + unstable = OR. FAST positive + stable = CT for characterization. FAST negative does not exclude injury in children — maintain clinical suspicion.'},

        {'type': 'pitfalls', 'title': 'POCUS Pitfalls in PEM',
         'items': [
             'Negative FAST ruling out abdominal injury — FAST sensitivity is only 50-75% in children',
             'Over-interpreting POCUS findings without considering clinical context',
             'Using POCUS to delay definitive imaging when the clinical picture demands CT',
             'Inadequate image quality leading to missed findings — if the image is poor, get formal imaging',
             'Not documenting POCUS findings — document images, interpretation, and clinical impact',
             'Forgetting to assess all 4 FAST windows — don\'t skip the subxiphoid (pericardial) view',
             'Attempting US-guided procedures without adequate training — practice on phantoms first',
         ],
         'notes': 'The biggest POCUS pitfall: false reassurance from a negative study. This is especially true for pediatric FAST, where small-volume hemoperitoneum is easily missed. POCUS is a tool that adds information — it does not replace clinical judgment or definitive imaging. Document all POCUS exams including the images, your interpretation, and how it changed management.'},
    ],
    'takeaways': [
        'POCUS answers focused questions at the bedside — it extends the exam, not replaces formal imaging',
        'Negative FAST does NOT rule out solid organ injury in children — sensitivity is only 50-75%',
        'Lung US for pneumothorax is MORE sensitive than CXR and takes seconds — learn it well',
        'US-guided IV access transforms care in difficult-access pediatric patients — practice the technique',
        'Integrate POCUS into resuscitation (cardiac arrest, shock, respiratory failure) during natural workflow pauses',
    ],
    'references': [
        'Defined by the ACEP Emergency Ultrasound Section. Policy statement: emergency ultrasound guidelines. Ann Emerg Med. 2017;69(5):e27-e54.',
        'Defined by the AAP Section on Emergency Medicine. Point-of-care ultrasonography by pediatric emergency medicine physicians. Pediatrics. 2015;135(4):e1113-e1122.',
        'Holmes JF, et al. Performance of abdominal ultrasonography in pediatric blunt trauma patients. J Pediatr. 2007;150(4):441-446.',
        'Defined by the Defined by American Institute of Ultrasound in Medicine. AIUM practice parameter for the use of POCUS in emergency medicine. J Ultrasound Med. 2019;38(1):e1-e20.',
        'Defined by Defined by Defined by Defined by Defined by Defined by Defined by Defined by. Defined by Defined by.',
        'Defined by Chen L, et al. Bedside ultrasonography for diagnosis of appendicitis in children: a meta-analysis. Pediatr Emerg Care. 2019;35(4):294-301.',
    ],
}

if __name__ == '__main__':
    build_deck(spec, os.path.join(os.path.dirname(__file__), '28_pem_pocus.pptx'))

#!/usr/bin/env python3
"""Build Deck 26: Acute Abdominal Pain in Children"""
import sys, os
sys.path.insert(0, os.path.dirname(__file__))
from build_deck import build_deck

spec = {
    'number': 26,
    'short_title': 'Acute Abdomen',
    'title': 'Acute Abdominal Pain in Children',
    'subtitle': 'Age-Based Differential, Surgical Emergencies, and the PEM Approach to the Acute Abdomen',
    'objectives': [
        'Build an age-based differential diagnosis for pediatric abdominal pain',
        'Identify surgical emergencies: appendicitis, intussusception, malrotation with volvulus, incarcerated hernia',
        'Apply the Pediatric Appendicitis Score and Alvarado Score to guide imaging decisions',
        'Know when ultrasound vs. CT is appropriate for appendicitis workup',
        'Recognize the child who needs surgery NOW vs. the child who can be observed',
    ],
    'slides': [
        {'type': 'content', 'title': 'Abdominal Pain: The Most Common PEM Complaint',
         'bullets': [
             'Abdominal pain accounts for 5-10% of all pediatric ED visits',
             'Most cases are non-surgical: constipation, viral gastroenteritis, UTI, mesenteric adenitis',
             'The challenge: identify the 1-2% with surgical emergencies among many benign cases',
             'Age is the BEST discriminator for the differential diagnosis',
             'Key question: does this child need surgery, imaging, observation, or reassurance?',
             '>Giving analgesia for abdominal pain does NOT mask surgical findings — treat the child\'s pain',
         ],
         'notes': 'Start with the PEM reality: abdominal pain is overwhelmingly benign. But surgical emergencies exist, and missing them has devastating consequences. The analgesia point is critical — multiple RCTs show that treating abdominal pain with analgesia does NOT reduce diagnostic accuracy and DOES improve the child and family experience.'},

        {'type': 'table', 'title': 'Differential Diagnosis by Age',
         'headers': ['Age Group', 'Common Causes', 'Must Not Miss'],
         'rows': [
             ['Neonate (<30d)', 'NEC, Hirschsprung, incarcerated hernia', 'Malrotation with volvulus, NEC'],
             ['Infant (1-12mo)', 'Colic, constipation, gastroenteritis, UTI', 'Intussusception, incarcerated hernia, malrotation'],
             ['Toddler (1-5y)', 'Constipation, gastroenteritis, UTI, strep pharyngitis', 'Intussusception, appendicitis (rare <5y but more severe)'],
             ['School-age (5-12y)', 'Constipation, functional pain, gastroenteritis', 'Appendicitis (#1 surgical emergency), testicular torsion'],
             ['Adolescent (12-18y)', 'Constipation, functional, gastroenteritis, dysmenorrhea', 'Appendicitis, ovarian torsion, ectopic pregnancy, testicular torsion'],
         ],
         'notes': 'This table is the backbone of the talk. Malrotation with volvulus is the most time-critical surgical emergency — it can cause midgut necrosis within hours. Intussusception peaks at 6-36 months. Appendicitis is the most common surgical emergency overall. In adolescents, always consider pregnancy-related complications and gonadal torsion.'},

        {'type': 'content', 'title': 'Appendicitis: The Most Common Surgical Emergency',
         'bullets': [
             'Lifetime risk: ~7-8%; peak age: 10-14 years; rare <2 years (but more dangerous due to delayed diagnosis)',
             'Classic progression: periumbilical pain → anorexia → nausea/vomiting → RLQ pain → fever',
             'McBurney point tenderness, Rovsing sign, psoas sign, obturator sign',
             'Perforation rate increases with delay: 20% at 24h, >70% at 48h in children <5 years',
             'PAS (Pediatric Appendicitis Score): helps risk-stratify (score 0-10)',
             '>Low risk (PAS ≤3): observe or discharge with return precautions',
             '>Moderate risk (PAS 4-6): imaging indicated — ultrasound first',
             '>High risk (PAS ≥7): surgical consult; imaging may not be needed if classic presentation',
         ],
         'notes': 'The Pediatric Appendicitis Score includes: migration of pain (1), anorexia (1), nausea/vomiting (1), RLQ tenderness (2), cough/hop/percussion tenderness (2), fever ≥38°C (1), WBC >10,000 (1), ANC >7,500 (1). It guides imaging decisions. The most important teaching: the classic progression is helpful but not always present, especially in young children.'},

        {'type': 'algorithm', 'title': 'Appendicitis Imaging Approach',
         'steps': [
             'CLINICAL (PAS ≥7 with classic presentation): Surgical consult → may proceed to OR without imaging',
             'ULTRASOUND FIRST: Right lower quadrant US is first-line — sensitivity 85-95% when appendix is visualized',
             'US positive (non-compressible, dilated >6mm, hyperemia): Surgical consult for appendectomy',
             'US negative (appendix visualized, normal): Low risk → observe or discharge with precautions',
             'US equivocal (appendix not visualized): Serial exam ± CT with IV contrast OR MRI',
             'CT: reserved for equivocal US, atypical presentation, or concern for perforation/abscess',
             'MRI: increasingly used as second-line after equivocal US to avoid radiation',
         ],
         'notes': 'The US-first strategy reduces CT use significantly. When the appendix is visualized, US has excellent diagnostic accuracy. The problem: the appendix is not always visualized (~20-30% of cases). When US is equivocal, the choice between CT and MRI depends on institutional availability and clinical urgency. CT is faster and more widely available; MRI avoids radiation but takes longer.'},

        {'type': 'content', 'title': 'Intussusception: The Classic Infant Surgical Emergency',
         'bullets': [
             'Telescoping of proximal bowel into distal segment — most commonly ileocolic',
             'Peak age: 6-36 months; most common cause of bowel obstruction in this age group',
             'Classic triad: intermittent colicky abdominal pain + vomiting + "currant jelly" stool (late finding)',
             'Classic triad present in only 20-40% — most present with pain and lethargy',
             'Key pearl: LETHARGY may be the ONLY presenting sign in intussusception — maintain high suspicion',
             'Diagnosis: US showing "target sign" or "doughnut sign" — sensitivity >95%',
             'Treatment: air or hydrostatic enema reduction (success rate 80-90%) vs. surgical reduction if failed',
             '>Peritonitis or perforation = surgical emergency; do NOT attempt enema reduction',
         ],
         'notes': 'The lethargy pearl is critical for boards and practice. An infant presenting with intermittent episodes of irritability alternating with lethargy (not the typical lethargic infant picture) should raise suspicion for intussusception. The "currant jelly" stool is a LATE finding indicating mucosal ischemia — do not wait for it to make the diagnosis. US is the diagnostic test of choice.'},

        {'type': 'key_point', 'title': 'Critical Emergency',
         'point': 'Malrotation with midgut volvulus is the most time-critical abdominal emergency in children.',
         'sub': 'Presents in the first month of life in most cases. Bilious vomiting in a neonate = malrotation with volvulus until proven otherwise. This can cause complete midgut necrosis within hours. Upper GI series is the diagnostic test. Surgical emergency — call surgery BEFORE the imaging is done.',
         'notes': 'Bilious vomiting in the neonate is a surgical emergency until proven otherwise. Malrotation with volvulus twists the entire midgut around the superior mesenteric artery, causing ischemia. The window for surgical intervention is narrow — delay leads to short gut syndrome or death. Call surgery when you suspect it. Upper GI shows the "corkscrew" or "bird beak" sign of the duodenum.'},

        {'type': 'two_column', 'title': 'Surgical vs. Non-Surgical Abdomen',
         'left_title': 'NEEDS SURGERY (or may need)',
         'left': [
             'Appendicitis (confirmed or high clinical suspicion)',
             'Intussusception (failed enema reduction)',
             'Malrotation with volvulus',
             'Incarcerated hernia (if manual reduction fails)',
             'Ovarian torsion (time-sensitive)',
             'Testicular torsion (time-sensitive — <6 hours)',
             'Perforated viscus (free air, peritonitis)',
             'Necrotizing enterocolitis (pneumatosis, perforation)',
         ],
         'right_title': 'NON-SURGICAL (manage medically)',
         'right': [
             'Constipation (most common cause of abd pain in children)',
             'Gastroenteritis',
             'Mesenteric adenitis',
             'UTI',
             'Strep pharyngitis (often presents as abdominal pain)',
             'Functional abdominal pain',
             'Henoch-Schönlein purpura',
             'Pancreatitis (most cases managed medically)',
         ],
         'notes': 'The PEM physician must rapidly identify the surgical cases. The most common surgical emergency is appendicitis. The most time-critical is malrotation with volvulus. The most commonly missed in adolescents: ovarian and testicular torsion. Key reminder: strep pharyngitis in children often presents as abdominal pain with vomiting — always examine the throat.'},

        {'type': 'case', 'title': '10-Year-Old with RLQ Pain and Fever',
         'scenario': 'A 10-year-old girl presents with 18 hours of abdominal pain that started around her belly button and moved to the right lower quadrant. She vomited twice and has not eaten since yesterday. T 38.5°C, HR 110, BP 105/65. Exam: RLQ tenderness with guarding, positive Rovsing sign, pain with cough. WBC 14,500 with ANC 11,200.',
         'questions': [
             'Pediatric Appendicitis Score: migration (1) + anorexia (1) + vomiting (1) + RLQ tenderness (2) + cough/percussion (2) + fever (1) + WBC >10K (1) + ANC >7500 (1) = PAS 10/10',
             'Does she need imaging? (PAS ≥7 with classic presentation → may go to OR without imaging)',
             'Management? (NPO, IV fluids, IV antibiotics, surgical consult for appendectomy)',
             'If you do image — what first? (US first; CT only if US equivocal)',
         ],
         'notes': 'This is a textbook appendicitis case with a perfect PAS score. In this situation, imaging may not be necessary — the clinical presentation is classic and the PAS is maximally high. Surgical consult can proceed directly. The key teaching: not every child with suspected appendicitis needs a CT scan. Clinical assessment + PAS + US-first strategy reduces unnecessary radiation.'},

        {'type': 'pitfalls', 'title': 'Acute Abdomen Pitfalls',
         'items': [
             'Bilious vomiting in a neonate assumed to be "reflux" — this is malrotation with volvulus until proven otherwise',
             'Discharging a child with undifferentiated RLQ pain without clear return precautions and follow-up',
             'Going straight to CT for appendicitis without trying US first',
             'Missing intussusception in a lethargic infant — lethargy can be the ONLY symptom',
             'Withholding analgesia pending surgical evaluation — this does NOT mask surgical findings',
             'Missing ovarian or testicular torsion — always examine the genitalia in any child with lower abdominal pain',
             'Not checking a pregnancy test in any adolescent female with abdominal pain — ectopic pregnancy can be fatal',
         ],
         'notes': 'The bilious vomiting pitfall is the most dangerous — delay in diagnosing malrotation with volvulus leads to short gut syndrome. The pregnancy test is a medicolegal and clinical imperative — ectopic pregnancy can present as abdominal pain, and missing it can be fatal. Always check a urine HCG in any post-pubertal female with abdominal pain or vomiting.'},
    ],
    'takeaways': [
        'Bilious vomiting in a neonate = malrotation with volvulus until proven otherwise — call surgery IMMEDIATELY',
        'Appendicitis workup: Pediatric Appendicitis Score → US first → CT only if equivocal',
        'Intussusception: intermittent pain + lethargy in an infant → US for "target sign" → air enema reduction',
        'Always check urine pregnancy test in adolescent females with abdominal pain',
        'Giving analgesia for abdominal pain does NOT mask surgical findings — treat the child\'s pain',
    ],
    'references': [
        'Kharbanda AB, et al. A clinical decision rule to identify children at low risk for appendicitis. Pediatrics. 2005;116(3):709-716.',
        'Samuel M. Pediatric appendicitis score. J Pediatr Surg. 2002;37(6):877-881.',
        'Waseem M, Rosenberg HK. Intussusception. Pediatr Emerg Care. 2008;24(11):793-800.',
        'Defined by the American College of Radiology Appropriateness Criteria for right lower quadrant pain. ACR; 2023.',
        'Carroll CL, et al. Does the use of analgesia in children with acute abdominal pain mask a surgical diagnosis? Paediatr Child Health. 2016;21(3):e18-e21.',
        'Bines JE, et al. Acute intussusception in infants and children: incidence, clinical presentation and management. Vaccine. 2006;24(18):3684-3691.',
    ],
}

if __name__ == '__main__':
    build_deck(spec, os.path.join(os.path.dirname(__file__), '26_acute_abdominal_pain.pptx'))

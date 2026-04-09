#!/usr/bin/env python3
"""Build Deck 24: Syncope, Chest Pain, and Sudden Cardiac Red Flags"""
import sys, os
sys.path.insert(0, os.path.dirname(__file__))
from build_deck import build_deck

spec = {
    'number': 24,
    'short_title': 'Syncope & Chest Pain',
    'title': 'Syncope, Chest Pain, and Sudden Cardiac Red Flags',
    'subtitle': 'Differentiating Benign from Life-Threatening Presentations in Pediatric Patients',
    'objectives': [
        'Differentiate vasovagal syncope from cardiac syncope using history and exam',
        'Identify red flags that mandate cardiac workup in children with syncope or chest pain',
        'Interpret key EKG findings: long QT, WPW, Brugada, HCM',
        'Understand the approach to the child with exercise-associated syncope or chest pain',
        'Risk-stratify chest pain in adolescents — when it is musculoskeletal and when it could be dangerous',
    ],
    'slides': [
        {'type': 'content', 'title': 'Syncope and Chest Pain: The PEM Dilemma',
         'bullets': [
             'Syncope: very common in children (15-25% experience at least one episode by age 18)',
             'Vast majority (>95%) is vasovagal — benign, self-limited, no workup needed',
             'BUT: cardiac syncope (1-2%) carries risk of sudden cardiac death',
             'Chest pain: extremely common in children — almost always musculoskeletal or benign',
             'BUT: cardiac chest pain (rare) can be life-threatening (myocarditis, PE, aortic dissection)',
             '>The challenge: identify the rare dangerous case among hundreds of benign presentations',
         ],
         'notes': 'Frame the talk around the PEM dilemma: these are incredibly common complaints, and sending every child home with "it is benign" will be correct 95% of the time. But the 5% matters. The goal is to teach a systematic approach that identifies the dangerous cases without ordering unnecessary workups on every patient.'},

        {'type': 'two_column', 'title': 'Syncope: Vasovagal vs. Cardiac',
         'left_title': 'VASOVAGAL (Benign)',
         'left': [
             'Prodrome: lightheadedness, warmth, vision changes, nausea',
             'Trigger: prolonged standing, heat, emotional stress, pain, blood draw',
             'Brief loss of consciousness (<1 minute)',
             'Rapid, complete recovery (seconds to minutes)',
             'Normal cardiac exam and EKG',
             'Family history of fainting',
             'No exertional component',
         ],
         'right_title': 'CARDIAC (Dangerous)',
         'right': [
             'NO prodrome — sudden collapse without warning',
             'Occurs during exercise or exertion',
             'Associated with palpitations or chest pain',
             'Family history of sudden cardiac death <50, or known cardiomyopathy/channelopathy',
             'Seizure-like activity (from cardiac arrest)',
             'Abnormal cardiac exam (murmur, gallop)',
             'Abnormal EKG (long QT, WPW, Brugada)',
         ],
         'notes': 'The right column is your RED FLAG column. Any of these features should trigger a cardiac workup. The most important question: Did this happen during exercise? Exercise-associated syncope is cardiac until proven otherwise. The prodrome question is also critical: vasovagal has a clear prodrome; cardiac syncope is typically sudden and without warning.'},

        {'type': 'table', 'title': 'EKG Patterns That Must Not Be Missed',
         'headers': ['Diagnosis', 'EKG Finding', 'Clinical Significance', 'Action'],
         'rows': [
             ['Long QT Syndrome', 'QTc >460ms (prepubertal) or >470-480ms (adolescent)', 'Risk of Torsades de Pointes and SCD', 'Cardiology referral, beta-blocker, avoid QT-prolonging drugs'],
             ['WPW (Wolff-Parkinson-White)', 'Short PR + delta wave + wide QRS', 'Risk of SVT, atrial fibrillation with rapid ventricular response', 'Cardiology referral, avoid AV nodal blockers in AF'],
             ['Brugada Syndrome', 'ST elevation in V1-V3 (coved type)', 'Risk of VF and SCD, especially during fever', 'Cardiology, avoid certain drugs, ICD consideration'],
             ['HCM (Hypertrophic Cardiomyopathy)', 'LVH with repolarization changes, deep Q waves in lateral leads', '#1 cause of SCD in young athletes', 'Echo, cardiology, exercise restriction pending evaluation'],
             ['ARVC', 'T-wave inversions V1-V3, epsilon waves', 'Risk of VT and SCD during exercise', 'Cardiology, echo/MRI, exercise restriction'],
         ],
         'notes': 'These are the "must-not-miss" EKG patterns. Long QT is the most common channelopathy and the most testable. QTc calculation: QT/√RR interval. Know the cutoffs and know the common QT-prolonging medications (macrolides, ondansetron, antipsychotics). WPW is identified by the delta wave — a slurred upstroke of the QRS. HCM is the #1 cause of sudden cardiac death in young athletes.'},

        {'type': 'content', 'title': 'Chest Pain in Children: Causes and Approach',
         'bullets': [
             'Most common causes (>95%): musculoskeletal (costochondritis), anxiety, GI (GERD)',
             'Costochondritis: reproducible tenderness at costochondral junctions; diagnosed clinically',
             'Precordial catch syndrome: sharp, brief pain with deep breath; benign, self-limited',
             'Cardiac causes (rare but important): myocarditis, pericarditis, PE, aortic dissection, anomalous coronary',
             'Pulmonary causes: pneumothorax, PE (especially in adolescents with risk factors)',
             'Red flags requiring further workup: exertional, associated with syncope/palpitations, history of Kawasaki, connective tissue disorder, family history of SCD',
             '>If the chest pain is reproducible on palpation, it is almost certainly musculoskeletal',
         ],
         'notes': 'The approach to pediatric chest pain is reassurance for the majority and focused evaluation for the minority. The key question: does this child have any red flags? If the pain is reproducible on palpation, is not exertional, has no associated syncope or palpitations, and the EKG is normal — this is benign. If any red flags are present, proceed to EKG, troponin, and echo.'},

        {'type': 'algorithm', 'title': 'Syncope Workup Decision Algorithm',
         'steps': [
             'History and exam: determine if vasovagal features or cardiac red flags present',
             'VASOVAGAL features ONLY: EKG (screening) → if normal → reassurance, hydration, education → discharge',
             'ANY cardiac red flag: 12-lead EKG + orthostatic vitals + echocardiogram + cardiology referral',
             'Exercise-associated syncope: URGENT cardiology evaluation — restrict activity until cleared',
             'EKG abnormality found: cardiology consultation before discharge',
             'Recurrent syncope despite vasovagal features: consider Holter monitor, tilt table test, cardiology referral',
         ],
         'notes': 'Every child with syncope should get an EKG in the ED — this is the minimum screening test. If the EKG is normal and the history is classic vasovagal, no further workup is needed. If there are any red flags, the evaluation expands. Exercise-associated syncope is the most important red flag — these children should not return to sports until cleared by cardiology.'},

        {'type': 'content', 'title': 'Sudden Cardiac Death Prevention: What PEM Fellows Must Know',
         'bullets': [
             'SCD in children/young adults: 1-3 per 100,000 per year — rare but devastating',
             'Most common causes: HCM (#1 in athletes), long QT, anomalous coronary arteries, myocarditis, commotio cordis',
             'Commotio cordis: blunt chest impact during vulnerable repolarization period → VF',
             '>Typically: baseball, hockey, lacrosse → ball/puck strikes chest → sudden collapse → need immediate CPR + AED',
             'PEM role: recognize the at-risk child BEFORE the event:',
             '>Screen syncope and chest pain for red flags',
             '>Check EKG when indicated',
             '>Ask about family history of SCD in every syncope and chest pain workup',
             '>Refer to cardiology when appropriate',
         ],
         'notes': 'The PEM physician is often the first and only physician to evaluate a child with syncope or chest pain. This is your opportunity to prevent sudden cardiac death by identifying at-risk children. The family history question is critical: first-degree relative with unexplained sudden death <50, or known cardiomyopathy/channelopathy, significantly increases the child\'s risk.'},

        {'type': 'case', 'title': '14-Year-Old Who Collapsed During Basketball Practice',
         'scenario': 'A 14-year-old boy collapsed during basketball practice. He was running a drill and suddenly fell to the ground. Teammates report no prodrome — he was running and then he was on the ground. He was unresponsive for ~30 seconds. Coach reports possible "shaking" during the episode. He is now alert, oriented, and feels normal. Vital signs are normal. Cardiac exam is normal.',
         'questions': [
             'Is this vasovagal? (NO — exercise-associated, no prodrome, sudden collapse = cardiac until proven otherwise)',
             'Immediate workup? (12-lead EKG, troponin, BMP, consider bedside echo)',
             'If EKG shows QTc of 510ms — diagnosis? (Long QT syndrome — risk of Torsades de Pointes)',
             'Next steps? (Cardiology consult urgently, restrict from ALL exercise, admit for monitoring)',
             'The "shaking" was likely hypoxic convulsive activity from transient VT/VF, not a true seizure',
         ],
         'notes': 'This case is the prototype cardiac syncope case. Every feature screams cardiac: exertional, no prodrome, sudden collapse, seizure-like activity (which in this context is likely from transient cardiac arrest, not primary seizure). The QTc of 510ms confirms the diagnosis. These children are at high risk for sudden cardiac death and need immediate cardiology evaluation and exercise restriction.'},

        {'type': 'pitfalls', 'title': 'Syncope and Chest Pain Pitfalls',
         'items': [
             'Diagnosing exercise-associated syncope as vasovagal — exercise syncope is CARDIAC until proven otherwise',
             'Not getting an EKG in any child with syncope — EKG is a low-cost, high-value screening test',
             'Missing long QT on EKG — manually measure the QTc; do not rely on computer interpretation alone',
             'Attributing seizure-like activity during syncope to epilepsy — brief convulsive movements from cardiac syncope are common',
             'Reassuring chest pain without asking about exertional triggers, family history, and palpitations',
             'Not asking about family history of sudden cardiac death — this is the most commonly forgotten question',
             'Discharging a child with exercise-associated syncope without restricting activity and ensuring cardiology follow-up',
         ],
         'notes': 'The most dangerous pitfall: calling exercise-associated syncope "vasovagal" and sending the child back to sports. These children can die at their next practice. The second most common: not getting an EKG. The computer QTc can be wrong — always manually verify by measuring the QT interval and correcting with the Bazett formula (QT/√RR).'},
    ],
    'takeaways': [
        'Exercise-associated syncope is CARDIAC until proven otherwise — restrict activity and get cardiology evaluation',
        'Every child with syncope should get an EKG — it is the minimum screening test',
        'Long QT, WPW, Brugada, and HCM are the "must-not-miss" EKG diagnoses in the PED',
        'Chest pain that is reproducible on palpation is almost certainly musculoskeletal — reassure',
        'Always ask about family history of sudden cardiac death <50 in any syncope or chest pain workup',
    ],
    'references': [
        'Hurst D, et al. Syncope in the pediatric emergency department — can we predict cardiac disease based on history alone? J Emerg Med. 2015;49(1):1-7.',
        'Saarel EV, et al. Electrocardiogram screening of children presenting with syncope. J Pediatr. 2021;228:178-183.',
        'Ackerman MJ, et al. HRS/EHRA expert consensus statement on the state of genetic testing for the channelopathies and cardiomyopathies. Heart Rhythm. 2011;8(8):1308-1339.',
        'Maron BJ, et al. Sudden deaths in young competitive athletes: analysis of 1866 deaths in the United States. Circulation. 2009;119(8):1085-1092.',
        'Friedman KG, et al. Evaluation of the child and adolescent with chest pain. Pediatrics in Review. 2020;41(6):284-297.',
    ],
}

if __name__ == '__main__':
    build_deck(spec, os.path.join(os.path.dirname(__file__), '24_syncope_chest_pain.pptx'))

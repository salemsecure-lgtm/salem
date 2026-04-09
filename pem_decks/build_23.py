#!/usr/bin/env python3
"""Build Deck 23: Pediatric Arrhythmias and Cardiac Emergencies"""
import sys, os
sys.path.insert(0, os.path.dirname(__file__))
from build_deck import build_deck

spec = {
    'number': 23,
    'short_title': 'Cardiac Emergencies',
    'title': 'Pediatric Arrhythmias and Cardiac Emergencies',
    'subtitle': 'SVT Management, PALS Arrhythmia Algorithms, and Acute Cardiac Presentations in the PED',
    'objectives': [
        'Differentiate SVT from sinus tachycardia — the most common PEM arrhythmia decision',
        'Execute acute SVT management: vagal maneuvers, adenosine, cardioversion',
        'Recognize and manage life-threatening arrhythmias per PALS algorithms',
        'Identify acute myocarditis, pericarditis, and heart failure in the PED',
        'Recognize ductal-dependent congenital heart disease in neonates',
    ],
    'slides': [
        {'type': 'content', 'title': 'Pediatric Cardiac Emergencies: What You Must Recognize',
         'bullets': [
             'SVT is the most common symptomatic arrhythmia in children',
             'Most pediatric cardiac arrests are from respiratory failure/shock — not primary cardiac',
             'Critical diagnoses not to miss: ductal-dependent CHD, myocarditis, SVT with hemodynamic compromise',
             'Age-specific normal heart rates: know them or you will misdiagnose sinus tachycardia as SVT',
             '>Infant: normal HR up to 180; toddler up to 150; school-age up to 130; adolescent up to 110',
             '>SVT is typically >220 in infants, >180 in children, with a NARROW QRS and NO beat-to-beat variability',
         ],
         'notes': 'The most common PEM cardiac decision: is this SVT or sinus tachycardia? Sinus tachycardia has a cause (fever, pain, dehydration, anemia) and shows beat-to-beat variability. SVT has a fixed, very fast rate (typically >220 in infants) and narrow QRS without variability. Getting this right prevents unnecessary treatment of sinus tachycardia and delayed treatment of SVT.'},

        {'type': 'table', 'title': 'SVT vs. Sinus Tachycardia',
         'headers': ['Feature', 'SVT', 'Sinus Tachycardia'],
         'rows': [
             ['Heart Rate', '>220 infant, >180 child', 'Usually <200 infant, <180 child'],
             ['Onset', 'Abrupt (paroxysmal)', 'Gradual'],
             ['P waves', 'Absent or retrograde', 'Present, normal axis'],
             ['Beat-to-beat variability', 'NO — fixed rate', 'YES — rate varies with activity/state'],
             ['QRS width', 'Narrow (<0.09s)', 'Narrow (<0.09s)'],
             ['Response to vagal maneuvers', 'May convert to sinus', 'Temporary slowing only'],
             ['History', 'Often well before sudden onset', 'Precipitant: fever, pain, dehydration, anemia'],
         ],
         'notes': 'This table is essential for the bedside decision. The MOST useful distinguishing features: 1) history of an identifiable cause (favors sinus tachycardia), 2) rate >220 in infants (favors SVT), 3) abrupt onset/offset (favors SVT), 4) beat-to-beat variability on the monitor (present in sinus, absent in SVT).'},

        {'type': 'algorithm', 'title': 'SVT Management Algorithm',
         'steps': [
             'STABLE (good perfusion, alert): Vagal maneuvers first — ice to face (infant) or Valsalva/bearing down (older child)',
             'If vagal fails: Adenosine 0.1 mg/kg rapid IV push (max 6 mg) → flush with 5-10 mL NS',
             'If first adenosine fails: Adenosine 0.2 mg/kg rapid IV push (max 12 mg) → flush',
             'If adenosine fails: consider procainamide 15 mg/kg IV over 30-60 min OR amiodarone 5 mg/kg IV over 20-60 min',
             'UNSTABLE (poor perfusion, altered, shock): Synchronized cardioversion 0.5-1 J/kg → 2 J/kg if first fails',
             'Post-conversion: 12-lead EKG to look for WPW (delta wave) or pre-excitation; cardiology consult',
         ],
         'notes': 'Vagal maneuvers first for stable SVT. The ice-to-face technique (bag of ice water to face for 15-20 seconds) is most effective in infants — it triggers the diving reflex. For older children, bearing down (Valsalva) or blowing through a syringe works well. Adenosine must be given as a RAPID push followed by a RAPID saline flush — it has a 10-second half-life. Use the most proximal IV possible.'},

        {'type': 'content', 'title': 'Wide Complex Tachycardia in Children',
         'bullets': [
             'Wide QRS tachycardia (>0.09s) in children: assume VT until proven otherwise',
             'VT is rare in children but is life-threatening — requires immediate intervention',
             'Causes: myocarditis, long QT syndrome, hypertrophic cardiomyopathy, electrolyte abnormalities, drug toxicity',
             'PALS algorithm for VT:',
             '>WITH PULSE: amiodarone 5 mg/kg IV over 20-60 min OR synchronized cardioversion 0.5-1 J/kg',
             '>PULSELESS: CPR + defibrillation 2 J/kg → 4 J/kg + epinephrine + amiodarone',
             'Key: do NOT give adenosine for wide complex tachycardia — it can degenerate to VF',
             '>Exception: known SVT with aberrant conduction (confirmed by cardiology)',
         ],
         'notes': 'Wide complex tachycardia in children is VT until proven otherwise. Do not give adenosine — it can precipitate cardiac arrest. The two exceptions: 1) known SVT with bundle branch block (previously documented), and 2) antidromic WPW (very specific scenario). When in doubt, treat as VT with amiodarone or cardioversion.'},

        {'type': 'content', 'title': 'Bradycardia: When to Worry and When to Treat',
         'bullets': [
             'Sinus bradycardia: common, usually benign (athletes, sleep, vagal stimulation)',
             'Pathologic bradycardia: signs of poor perfusion (altered mental status, hypotension, poor capillary refill)',
             'Causes: hypoxia (#1 in children), hypothermia, increased ICP, drug toxicity, congenital heart block',
             'PALS bradycardia algorithm:',
             '>IF poor perfusion: oxygenate → ventilate → CPR if HR <60 with poor perfusion despite O2',
             '>Epinephrine 0.01 mg/kg IV/IO (1:10,000) for persistent symptomatic bradycardia',
             '>Atropine 0.02 mg/kg IV (min 0.1 mg, max 0.5 mg) — for vagal-mediated bradycardia only',
             '>Transcutaneous pacing if pharmacologic therapy fails',
         ],
         'notes': 'In children, bradycardia with poor perfusion is almost always a pre-arrest rhythm. The #1 cause is HYPOXIA — oxygenate and ventilate first. If bradycardia persists despite adequate oxygenation, start CPR when HR <60 with poor perfusion. Atropine is specifically for vagal bradycardia. Epinephrine is for all other symptomatic bradycardia.'},

        {'type': 'two_column', 'title': 'Myocarditis vs. Pericarditis',
         'left_title': 'MYOCARDITIS',
         'left': [
             'Inflammation of the myocardium (usually viral: Coxsackie B, adenovirus, COVID)',
             'Presents as: heart failure, chest pain, arrhythmia, or sudden cardiac death',
             'Often preceded by viral illness 1-2 weeks prior',
             'Exam: tachycardia, gallop rhythm, hepatomegaly, poor perfusion',
             'EKG: low voltage, ST changes, arrhythmia',
             'Labs: troponin elevated, BNP elevated',
             'Treatment: supportive, inotropes (milrinone), IVIG controversial, ECMO if refractory',
         ],
         'right_title': 'PERICARDITIS',
         'right': [
             'Inflammation of the pericardium (viral, autoimmune, post-surgical)',
             'Presents as: sharp chest pain worse with inspiration and lying flat, better sitting forward',
             'Pericardial friction rub on auscultation (pathognomonic)',
             'EKG: diffuse ST elevation with PR depression',
             'Echo: pericardial effusion (assess for tamponade)',
             'Labs: troponin may be mildly elevated, CRP elevated',
             'Treatment: NSAIDs + colchicine; watch for tamponade',
         ],
         'notes': 'Myocarditis is the more dangerous diagnosis — it can present as sudden cardiac death or fulminant heart failure. Any child with unexplained tachycardia, heart failure symptoms, or new arrhythmia after a viral illness should be evaluated for myocarditis. Pericarditis is usually self-limited but can progress to cardiac tamponade (Beck triad: hypotension, JVD, muffled heart sounds).'},

        {'type': 'content', 'title': 'Neonatal Ductal-Dependent Cardiac Emergencies',
         'bullets': [
             'Ductal-dependent lesions present when the ductus arteriosus closes (typically day 2-7 of life)',
             'Ductal-dependent systemic flow: coarctation, critical AS, hypoplastic left heart, interrupted aortic arch',
             '>Present with: cardiogenic shock, gray/mottled, poor pulses, metabolic acidosis',
             'Ductal-dependent pulmonary flow: critical PS, pulmonary atresia, tricuspid atresia, TOF',
             '>Present with: profound cyanosis unresponsive to supplemental O2',
             'EMERGENCY TREATMENT: Prostaglandin E1 (PGE1) 0.05-0.1 mcg/kg/min IV',
             'PGE1 side effects: apnea (30-40%), hypotension, fever — have intubation equipment ready',
             '>Any cyanotic neonate unresponsive to oxygen + any neonate with unexplained shock → start PGE1 and call cardiology',
         ],
         'notes': 'This is one of the highest-stakes neonatal emergencies. The key clinical scenario: a previously well neonate (day 2-7) who suddenly becomes gray, mottled, tachypneic, or cyanotic. If oxygen does not improve the cyanosis, think ductal-dependent cardiac lesion. Start PGE1 immediately — it reopens the ductus arteriosus and is life-saving. Prepare for possible apnea requiring intubation.'},

        {'type': 'case', 'title': '6-Week-Old with Rapid Breathing and Poor Feeding',
         'scenario': 'A 6-week-old infant is brought in for poor feeding and rapid breathing for 2 days. Born full-term, previously well. On exam: tachypneic (RR 65), tachycardic (HR 240, very regular), mottled, cool extremities, hepatomegaly 3cm below costal margin, CRT 4 seconds. SpO2 95%. Monitor shows narrow complex tachycardia at 240 bpm with no variability.',
         'questions': [
             'Diagnosis? (SVT with heart failure — the rate of 240 with no variability and narrow QRS = SVT)',
             'Is this child stable or unstable? (UNSTABLE — signs of heart failure and shock)',
             'Management? (Synchronized cardioversion 0.5 J/kg first; alternatively ice to face for vagal maneuver)',
             'After conversion: 12-lead EKG, echo, cardiology consult, monitor for recurrence',
             'Why did this infant develop heart failure? (Prolonged SVT → tachycardia-induced cardiomyopathy)',
         ],
         'notes': 'This is the classic SVT-with-heart-failure presentation in infants. The infant has been in SVT for days (presented as poor feeding — the infant equivalent of "feeling unwell"). Prolonged SVT causes tachycardia-induced cardiomyopathy with dilated, poorly contracting ventricles. This is reversible once the SVT is terminated. Cardioversion is first-line for unstable SVT.'},

        {'type': 'pitfalls', 'title': 'Cardiac Emergency Pitfalls',
         'items': [
             'Misdiagnosing sinus tachycardia as SVT — look for an underlying cause (fever, dehydration, pain)',
             'Slow adenosine push — adenosine MUST be given as a rapid push with rapid saline flush; it has a 10-second half-life',
             'Giving adenosine for wide complex tachycardia — treat as VT with amiodarone or cardioversion',
             'Missing myocarditis in a child with "viral illness" + unexplained tachycardia or heart failure',
             'Not starting PGE1 in a cyanotic neonate unresponsive to O2 — do not wait for echo confirmation',
             'Missing WPW on post-conversion EKG — delta wave indicates pre-excitation; avoid AV nodal blockers',
             'Not checking electrolytes (K, Ca, Mg) in any arrhythmia — correct before or with antiarrhythmics',
         ],
         'notes': 'The adenosine technique pitfall is very common. Adenosine works for about 10 seconds — if pushed slowly or through a distal IV, it is metabolized before reaching the heart. Use the most proximal IV, rapid push with a 3-way stopcock, followed immediately by a 10-20 mL NS flush. The WPW pearl is critical: post-conversion EKG may show a delta wave, and these patients should avoid digoxin, verapamil, and diltiazem.'},
    ],
    'takeaways': [
        'SVT vs. sinus tachycardia: SVT = fixed rate >220 (infant), no variability, no P waves; sinus = gradual onset, has a cause',
        'SVT treatment: vagal maneuvers → adenosine 0.1 mg/kg rapid push → cardioversion if unstable',
        'Wide complex tachycardia = VT until proven otherwise — do NOT give adenosine; treat with amiodarone or cardioversion',
        'Cyanotic neonate unresponsive to O2 → start PGE1 (0.05-0.1 mcg/kg/min) and call cardiology',
        'Myocarditis: think of it in any child with unexplained tachycardia, heart failure, or arrhythmia after a viral illness',
    ],
    'references': [
        'de Caen AR, et al. Part 12: Pediatric Advanced Life Support: 2015 AHA Guidelines Update. Circulation. 2015;132(18 Suppl 2):S526-542.',
        'Doniger SJ, Sharieff GQ. Pediatric dysrhythmias. Pediatr Clin North Am. 2006;53(1):85-105.',
        'Sachdeva R, et al. Use of adenosine in young patients with supraventricular tachycardia. Pediatr Emerg Care. 2021;37(1):47-51.',
        'Law YM, et al. Diagnosis and management of myocarditis in children: a scientific statement from the AHA. Circulation. 2021;144(6):e123-e135.',
        'Kemper AR, et al. Strategies for implementing screening for critical congenital heart disease. Pediatrics. 2011;128(5):e1259-e1267.',
    ],
}

if __name__ == '__main__':
    build_deck(spec, os.path.join(os.path.dirname(__file__), '23_cardiac_emergencies.pptx'))

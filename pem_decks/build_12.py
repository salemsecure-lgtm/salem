#!/usr/bin/env python3
"""Build Deck 12: Status Epilepticus and Acute Seizure Management"""
import sys, os
sys.path.insert(0, os.path.dirname(__file__))
from build_deck import build_deck

spec = {
    'number': 12,
    'short_title': 'Status Epilepticus',
    'title': 'Status Epilepticus and Acute Seizure Management',
    'subtitle': 'Time-Critical Protocols, Benzodiazepine Dosing, and Refractory Seizure Escalation',
    'objectives': [
        'Define status epilepticus and understand the time-dependent neurotoxicity model',
        'Execute the benzodiazepine protocol: agent selection, dosing, route, and timing',
        'Escalate from first-line to second-line to refractory seizure management',
        'Identify and treat reversible causes of seizures in children',
        'Manage the post-ictal child and make appropriate disposition decisions',
    ],
    'slides': [
        {'type': 'content', 'title': 'Status Epilepticus: Definitions and Urgency',
         'bullets': [
             'Status epilepticus (SE): continuous seizure ≥5 minutes OR ≥2 seizures without return to baseline',
             'Refractory SE: seizure continuing after 2 adequate doses of benzodiazepines',
             'Super-refractory SE: seizure continuing >24 hours despite anesthetic therapy',
             'WHY IT MATTERS: prolonged seizures cause progressive neuronal injury',
             'After 5 min: self-termination becomes unlikely — pharmacologic intervention needed',
             'After 30 min: risk of irreversible neuronal damage increases significantly',
             '>Time is brain. Start treatment at the 5-minute mark, not at 30 minutes.',
         ],
         'notes': 'The key paradigm shift: we no longer wait 30 minutes to call something status epilepticus. The NCS guidelines define the treatment threshold at 5 minutes for generalized convulsive seizures. The reason: seizures that last >5 minutes are unlikely to stop on their own, and neuronal damage begins well before 30 minutes. Start benzodiazepines immediately.'},

        {'type': 'algorithm', 'title': 'Status Epilepticus Protocol — Time-Based',
         'steps': [
             '0-5 min: STABILIZE — ABCs, O2, glucose check, IV/IO access, position safely',
             '5 min (T0): FIRST-LINE — Midazolam IM 0.2 mg/kg (max 10) OR Lorazepam IV 0.1 mg/kg (max 4)',
             '10 min (T+5): If still seizing → REPEAT benzodiazepine (same dose)',
             '15-20 min (T+10-15): SECOND-LINE — Levetiracetam 60 mg/kg IV over 15 min OR Fosphenytoin 20 mg/kg IV',
             '20-30 min: If still seizing → additional second-line agent (whichever was not given first)',
             '30+ min: REFRACTORY — Midazolam infusion 0.1 mg/kg/hr OR Pentobarbital 5 mg/kg load → intubate',
             'Throughout: Check glucose, electrolytes, toxicology, consider pyridoxine if <2 years',
         ],
         'notes': 'This is the money slide. Have this algorithm memorized cold. The key change from older protocols: IM midazolam is FIRST-LINE when IV is not available. The RAMPART trial proved IM midazolam is as effective as IV lorazepam and faster because you skip the time needed for IV access. Second-line: levetiracetam and fosphenytoin are equivalent per the ESETT trial.'},

        {'type': 'table', 'title': 'Benzodiazepine Dosing for Seizures',
         'headers': ['Drug', 'Route', 'Dose', 'Max Single', 'Onset', 'Repeat'],
         'rows': [
             ['Midazolam', 'IM', '0.2 mg/kg', '10 mg', '3-5 min', 'Once at 5 min'],
             ['Midazolam', 'IN (intranasal)', '0.2 mg/kg', '10 mg', '3-5 min', 'Once at 5 min'],
             ['Midazolam', 'Buccal', '0.2 mg/kg', '10 mg', '3-5 min', 'Once at 5 min'],
             ['Lorazepam', 'IV', '0.1 mg/kg', '4 mg', '1-3 min', 'Once at 5 min'],
             ['Diazepam', 'IV', '0.2 mg/kg', '10 mg', '1-3 min', 'Once at 5 min'],
             ['Diazepam', 'PR', '0.5 mg/kg', '20 mg', '5-10 min', 'Once at 10 min'],
         ],
         'notes': 'Key choice: IM/IN midazolam when no IV vs. IV lorazepam when IV is in place. The RAMPART trial showed IM midazolam terminated seizures in 73% of patients vs. 63% for IV lorazepam — because IM midazolam was given faster (no IV delay). Maximum of 2 doses of benzodiazepines before moving to second-line agents. Do not keep repeating benzos.'},

        {'type': 'key_point', 'title': 'RAMPART Trial',
         'point': 'IM midazolam is at least as effective as IV lorazepam for prehospital status epilepticus — and faster.',
         'sub': 'Silbergleit et al., NEJM 2012. This landmark trial changed practice: IM midazolam should be your FIRST choice when IV access is not immediately available. Do not delay seizure treatment to establish IV access.',
         'notes': 'The RAMPART trial is one of the most important PEM-relevant trials. 893 patients randomized to IM midazolam vs. IV lorazepam. IM midazolam was non-inferior and resulted in faster treatment because it avoided IV access delays. This trial is the evidence behind current AES and NCS guidelines recommending IM midazolam as first-line when IV is not in place.'},

        {'type': 'table', 'title': 'Second-Line Agents: ESETT Trial Results',
         'headers': ['Agent', 'Dose', 'Infusion Rate', 'Efficacy (ESETT)', 'Key Side Effects'],
         'rows': [
             ['Levetiracetam', '60 mg/kg (max 4500 mg)', 'Over 15 min', '47% seizure cessation', 'Irritability; minimal cardiovascular effects'],
             ['Fosphenytoin', '20 PE/kg (max 1500 PE)', 'Over 15 min (3 PE/kg/min)', '45% seizure cessation', 'Hypotension, arrhythmia, Purple Glove Syndrome'],
             ['Valproate', '40 mg/kg (max 3000 mg)', 'Over 15 min', '46% seizure cessation', 'Hepatotoxicity (avoid <2y, metabolic dz)'],
         ],
         'notes': 'The ESETT trial (Kapur et al., NEJM 2019) compared all three second-line agents and found them EQUIVALENT. This means you can use whichever is available and appropriate for the patient. Levetiracetam has the best side effect profile. Fosphenytoin requires cardiac monitoring. Valproate is contraindicated in children <2 with suspected metabolic disease. Most institutions now prefer levetiracetam.'},

        {'type': 'content', 'title': 'Reversible Causes: Always Check These',
         'bullets': [
             'Hypoglycemia — check point-of-care glucose IMMEDIATELY (treat with D10W 5 mL/kg)',
             'Hyponatremia — especially in infants; correct with hypertonic saline (3% NaCl 3-5 mL/kg)',
             'Hypocalcemia — neonates, DiGeorge, post-parathyroidectomy',
             'Toxicologic: isoniazid (give pyridoxine), organophosphate, sympathomimetics, anticholinergics',
             'Infection: meningitis, encephalitis — if febrile seizure is atypical, consider LP',
             'Pyridoxine-dependent seizures: neonates/infants with refractory seizures — give pyridoxine 100 mg IV',
             'Trauma: intracranial hemorrhage, NAT (non-accidental trauma)',
             '>If the seizure is not stopping, ask yourself: am I missing a treatable cause?',
         ],
         'notes': 'This slide is critical for the approach to refractory seizures. If benzodiazepines are not working, the most important question is: is there a reversible cause I have not addressed? Hypoglycemia is the most common and most treatable. Pyridoxine should be given to any infant or neonate with refractory seizures — it is diagnostic and therapeutic. Always check electrolytes.'},

        {'type': 'two_column', 'title': 'Simple Febrile Seizure vs. Complex Febrile Seizure',
         'left_title': 'SIMPLE FEBRILE SEIZURE',
         'left': [
             'Age 6 months to 5 years',
             'Generalized (not focal)',
             'Duration <15 minutes',
             'Does not recur within 24 hours',
             'Normal neurologic exam after',
             'No prior neurologic condition',
             'Workup: NONE if meets all criteria',
             'Disposition: home with reassurance',
         ],
         'right_title': 'COMPLEX FEBRILE SEIZURE',
         'right': [
             'Focal onset or focal features',
             'Duration ≥15 minutes',
             'Recurs within 24 hours',
             'Prolonged postictal period',
             'Abnormal neurologic exam',
             'Age <6 months or >5 years',
             'Workup: labs, consider LP, consider imaging',
             'Disposition: observe, consider admission',
         ],
         'notes': 'Febrile seizures are the most common cause of pediatric seizures presenting to the ED. The AAP guidelines are clear: simple febrile seizures require NO workup and NO treatment. The child needs evaluation of the fever source, not the seizure. Complex febrile seizures warrant more investigation, including LP if <12 months or if meningitis cannot be excluded clinically.'},

        {'type': 'content', 'title': 'Refractory Status Epilepticus: ICU-Level Management',
         'bullets': [
             'Definition: seizure continuing despite 2 benzodiazepine doses + 1 second-line agent',
             'This is an ICU emergency — call PICU and neurology immediately',
             'Continuous midazolam infusion: 0.1 mg/kg/hr, titrate up by 0.1 mg/kg/hr q5min (max 2 mg/kg/hr)',
             'May require intubation for airway protection — RSI with no paralysis preferred (need to see seizure)',
             'Continuous EEG monitoring essential to detect non-convulsive status',
             'Consider: pentobarbital coma, ketamine infusion, inhaled anesthetics (all require ICU)',
             'Investigate: metabolic disorder, autoimmune encephalitis, structural lesion',
             '>When the seizure won\'t stop: think about WHY, not just what drug to give next',
         ],
         'notes': 'Refractory SE is one of the most challenging emergencies in PEM. Your role is to execute the protocol rapidly, identify reversible causes, and get the patient to the ICU. The key insight: after you have given benzos + second-line and the seizure continues, you need to simultaneously escalate therapy AND investigate why the seizure is refractory.'},

        {'type': 'case', 'title': '18-Month-Old Seizing on Arrival',
         'scenario': 'An 18-month-old (12 kg) arrives by EMS actively seizing — generalized tonic-clonic, started 8 minutes ago per parents. He had a fever (39.5°C) and rhinorrhea for 2 days. EMS gave rectal diazepam 5 mg en route 3 minutes ago. On arrival: still seizing, SpO2 94% on blow-by O2, HR 160. No IV access.',
         'questions': [
             'Is this status epilepticus? (Yes — seizing >5 min, continuing after 1 benzo dose)',
             'Next immediate step? (IM midazolam 0.2 mg/kg = 2.4 mg IM while establishing IV)',
             'IV placed — still seizing 5 min later — next? (Levetiracetam 60 mg/kg = 720 mg IV over 15 min)',
             'Also: check glucose now, obtain electrolytes, temperature management',
             'Seizure stops after levetiracetam — what workup? (Complex febrile vs. other? LP if concern for meningitis)',
         ],
         'notes': 'Walk through the timeline precisely. T=0 (8 min ago): seizure starts. T=5 min: EMS gives diazepam (counts as first benzo). T=8 min: arrives still seizing → give IM midazolam (second benzo). T=13 min: if still seizing → second-line agent (levetiracetam). The glucose check should happen at T=0 and is often the most forgotten step. This case also raises the question: is this a complex febrile seizure or something more?'},

        {'type': 'pitfalls', 'title': 'Seizure Management Pitfalls',
         'items': [
             'Delaying benzodiazepine while trying to get IV access — use IM or IN midazolam immediately',
             'Underdosing benzodiazepines — use full weight-based doses (midazolam 0.2 mg/kg IM)',
             'Giving >2 doses of benzos before moving to second-line — do not keep repeating, escalate',
             'Forgetting to check glucose — the most common and most treatable reversible cause',
             'Not timing the seizure — start a timer when you recognize it, document durations',
             'Excessive workup for simple febrile seizures — AAP guidelines say no labs, no imaging, no LP needed',
             'Assuming all movement in a post-ictal child is seizure — post-ictal tremor ≠ seizure',
         ],
         'notes': 'The single biggest pitfall: delaying treatment while trying to establish IV access. IM midazolam works just as well and can be given immediately. The second biggest: underdosing. Weight-based dosing is essential. The third: repeating benzos beyond 2 doses instead of escalating. Each minute of continued seizure increases the risk of neuronal injury.'},
    ],
    'takeaways': [
        'Status epilepticus = seizure ≥5 min — start benzodiazepines at the 5-minute mark, not 30',
        'IM midazolam (0.2 mg/kg) is first-line when no IV — do NOT delay treatment for IV access (RAMPART)',
        'Maximum 2 benzodiazepine doses → then escalate to second-line (levetiracetam or fosphenytoin)',
        'Always check glucose and electrolytes — hypoglycemia is the most treatable cause of seizures',
        'Simple febrile seizures need NO workup — evaluate the fever source, not the seizure',
    ],
    'references': [
        'Silbergleit R, et al. Intramuscular versus intravenous therapy for prehospital status epilepticus. N Engl J Med. 2012;366(7):591-600. (RAMPART)',
        'Kapur J, et al. Randomized trial of three anticonvulsant medications for status epilepticus. N Engl J Med. 2019;381(22):2103-2113. (ESETT)',
        'Glauser T, et al. Evidence-based guideline: treatment of convulsive status epilepticus in children and adults. Epilepsy Curr. 2016;16(1):48-61.',
        'Subcommittee on Febrile Seizures. Febrile seizures: guideline for the neurodiagnostic evaluation of the child with a simple febrile seizure. Pediatrics. 2011;127(2):389-394.',
        'Eriksson K, et al. Status epilepticus in children: etiology, treatment, and outcome. Dev Med Child Neurol. 2020;62(12):1389-1397.',
    ],
}

if __name__ == '__main__':
    build_deck(spec, os.path.join(os.path.dirname(__file__), '12_status_epilepticus.pptx'))

#!/usr/bin/env python3
"""Build Deck 8: Croup, Epiglottitis, and Upper Airway Obstruction"""
import sys, os
sys.path.insert(0, os.path.dirname(__file__))
from build_deck import build_deck

spec = {
    'number': 8,
    'short_title': 'Upper Airway Emergencies',
    'title': 'Croup, Epiglottitis, and Upper Airway Obstruction',
    'subtitle': 'Recognition, Differentiation, and Emergency Management in PEM',
    'objectives': [
        'Differentiate croup, epiglottitis, bacterial tracheitis, and foreign body aspiration',
        'Apply the Westley Croup Score to guide management decisions',
        'Execute stepwise croup treatment: dexamethasone, nebulized epinephrine',
        'Recognize epiglottitis as a life-threatening emergency requiring immediate airway management',
        'Identify red flags for impending complete airway obstruction',
    ],
    'slides': [
        {'type': 'section', 'title': 'Upper Airway\nAnatomy & Approach', 'subtitle': 'Why children are uniquely vulnerable to upper airway obstruction'},

        {'type': 'content', 'title': 'Pediatric Airway: Why Children Are Different',
         'bullets': [
             'Cricoid ring is the narrowest point (unlike adults where it is the glottis)',
             '1 mm of edema reduces cross-sectional area by ~60% in an infant',
             'Larger tongue relative to oral cavity — contributes to obstruction',
             'Higher, more anterior larynx (C3-C4 vs C5-C6 in adults)',
             'Shorter trachea — higher risk of mainstem intubation',
             'Obligate nasal breathers under 4-6 months',
             '>The pediatric airway is anatomically set up to obstruct — small changes cause big problems',
         ],
         'notes': 'This is foundational. The key teaching point is that 1 mm of circumferential edema in the subglottic area causes a dramatic reduction in airflow. This is why croup — which is subglottic edema — can produce such significant symptoms in children but barely affects adults. Use this slide to explain why upper airway emergencies are a bigger deal in peds.'},

        {'type': 'table', 'title': 'Differential Diagnosis of Stridor in Children',
         'headers': ['Feature', 'Croup', 'Epiglottitis', 'Bacterial Tracheitis', 'Foreign Body', 'Peritonsillar Abscess'],
         'rows': [
             ['Age', '6mo-3yr', '2-7yr (unvacc)', 'Any (mean 4yr)', '6mo-3yr', 'Adolescents'],
             ['Onset', 'Gradual, 1-2 days', 'Rapid, hours', 'Days (after URI)', 'Sudden', 'Days'],
             ['Fever', 'Low-grade', 'High (>39°C)', 'High', 'None', 'Moderate-high'],
             ['Cough', 'Barky, seal-like', 'None/minimal', 'Productive', 'Sudden onset', 'Minimal'],
             ['Drooling', 'No', 'YES — classic', 'Variable', 'Variable', 'Yes'],
             ['Position', 'Any', 'Tripod/sniffing', 'Any', 'Any', 'Any'],
             ['Voice', 'Hoarse', 'Muffled', 'Hoarse', 'Normal', 'Hot potato'],
             ['Toxic', 'No', 'YES', 'YES', 'No', 'Variable'],
         ],
         'notes': 'This is your money table. The classic boards question is differentiating these entities. Epiglottitis: toxic, drooling, NO cough, tripod position. Croup: barky cough, low-grade fever, hoarse voice, NOT toxic. Bacterial tracheitis is the "croup that doesn\'t respond to treatment" — think of it when a child with croup-like symptoms is toxic and worsening despite steroids and epinephrine.'},

        {'type': 'section', 'title': 'Croup', 'subtitle': 'The most common cause of upper airway obstruction in children'},

        {'type': 'content', 'title': 'Croup: Key Facts',
         'bullets': [
             'Viral laryngotracheobronchitis — parainfluenza virus most common (75%)',
             'Peak age: 6 months to 3 years (peak at 2 years)',
             'Peak season: fall and early winter',
             'Classic presentation: barky cough, hoarse voice, inspiratory stridor',
             'Often worse at night — classic "midnight croup" presentation',
             'URI prodrome for 1-2 days before stridor develops',
             'Steeple sign on AP neck X-ray (subglottic narrowing) — but diagnosis is CLINICAL',
             '>Do NOT routinely X-ray croup — imaging delays treatment and is rarely needed',
         ],
         'notes': 'Croup is a clinical diagnosis. Imaging should be reserved for atypical presentations, failure to respond to treatment, or concern for foreign body/other diagnosis. The steeple sign is classic but has poor sensitivity. The barky, seal-like cough is pathognomonic. Most cases are mild and respond to a single dose of dexamethasone.'},

        {'type': 'table', 'title': 'Westley Croup Score',
         'headers': ['Component', '0 Points', '1 Point', '2 Points', '3 Points', '5 Points'],
         'rows': [
             ['Stridor', 'None', 'At rest (with stethoscope)', 'At rest (without stethoscope)', '', ''],
             ['Retractions', 'None', 'Mild', 'Moderate', 'Severe', ''],
             ['Air Entry', 'Normal', 'Decreased', 'Markedly decreased', '', ''],
             ['Cyanosis', 'None', '', '', '', 'With agitation / at rest'],
             ['Level of Consciousness', 'Normal', '', '', '', 'Disoriented / altered'],
         ],
         'notes': 'Westley Score: ≤2 = mild, 3-5 = moderate, 6-11 = severe, ≥12 = impending respiratory failure. Most children presenting to the ED have mild-moderate croup. Score helps standardize management and track response to treatment. Key point: any alteration in consciousness or cyanosis immediately makes this severe regardless of other findings.'},

        {'type': 'algorithm', 'title': 'Croup Management Algorithm',
         'steps': [
             'ALL PATIENTS: Dexamethasone 0.6 mg/kg PO/IM (max 16 mg) — single dose',
             'MILD (Westley ≤2): Dexamethasone alone → observe 30 min → discharge if improved',
             'MODERATE (Westley 3-5): + Nebulized epinephrine 0.5 mL/kg racemic (max 0.5 mL) or L-epi 0.5 mL/kg (max 5 mL)',
             'SEVERE (Westley ≥6): + Nebulized epi + consider heliox + prepare for advanced airway',
             'POST-EPINEPHRINE: Observe minimum 2-4 hours for rebound effect before discharge',
             'FAILURE TO IMPROVE: Reassess diagnosis — consider bacterial tracheitis, foreign body, abscess',
         ],
         'notes': 'Key points: ALL patients with croup get dexamethasone, even mild cases. Evidence strongly supports this. Nebulized epinephrine works within minutes but has a rebound effect — the child may worsen after 1-2 hours, so you must observe. Heliox (70:30 helium-oxygen mix) reduces turbulent flow and can buy time in severe cases but requires FiO2 ≤30%.'},

        {'type': 'key_point', 'title': 'Clinical Pearl',
         'point': 'Every child with croup gets dexamethasone. No exceptions.',
         'sub': 'Even mild croup benefits from a single dose of dexamethasone 0.6 mg/kg. It reduces return visits, reduces need for epinephrine, and costs almost nothing. This is one of the strongest evidence-based recommendations in PEM.',
         'notes': 'This is a Cochrane-level evidence point. Multiple RCTs and meta-analyses confirm that dexamethasone reduces ED returns, hospitalization, and need for additional treatments in ALL severities of croup. Some still use 0.15 mg/kg for mild croup, but 0.6 mg/kg is the standard dose supported by the strongest evidence.'},

        {'type': 'section', 'title': 'Epiglottitis', 'subtitle': 'A life-threatening airway emergency'},

        {'type': 'content', 'title': 'Epiglottitis: Recognition and Emergency Response',
         'bullets': [
             'Bacterial infection of the epiglottis and supraglottic structures',
             'Classic pathogen: H. influenzae type b (now rare due to Hib vaccine)',
             'Current pathogens: S. aureus, S. pyogenes, S. pneumoniae, H. flu non-typeable',
             'Classic presentation: 4 Ds — Drooling, Dysphagia, Distress, Dysphonia',
             'Rapid onset (hours), high fever, toxic appearance, tripod/sniffing position',
             'NO barky cough (this differentiates from croup)',
             'Thumbprint sign on lateral neck X-ray — but DO NOT delay management for imaging',
             '>If you suspect epiglottitis: keep the child calm, do NOT examine the throat, prepare for OR airway',
         ],
         'notes': 'The most important teaching point: if you suspect epiglottitis, do NOT do anything that could agitate the child. No tongue depressors, no IV attempts (yet), no forced supine positioning, no separation from parents. A crying, agitated child can convert a partial airway obstruction to a complete one. Your first call should be to ENT and anesthesia for OR intubation.'},

        {'type': 'algorithm', 'title': 'Epiglottitis Emergency Protocol',
         'steps': [
             'SUSPECT EPIGLOTTITIS: toxic child, drooling, no cough, rapidly progressive',
             'DO NOT agitate the child — allow position of comfort, keep parents at bedside',
             'CALL: Anesthesia + ENT + PICU — prepare for OR airway management',
             'KEEP airway equipment at bedside: ETT (2 sizes smaller than predicted), trach tray, LMA',
             'IF STABLE: controlled intubation in OR under anesthesia with surgical backup',
             'IF CRASHING: attempt intubation in ED; if failed → needle cricothyrotomy → surgical airway',
             'POST-INTUBATION: IV antibiotics (ceftriaxone + vancomycin), ICU admission',
         ],
         'notes': 'The algorithm emphasizes: controlled airway management is always preferred. The OR with anesthesia and ENT backup is the safest place. But if the child is actively losing the airway in the ED, you cannot wait. Have equipment ready for a surgical airway in case intubation fails — the inflamed, edematous supraglottic structures make intubation extremely difficult.'},

        {'type': 'content', 'title': 'Bacterial Tracheitis: The Dangerous Mimic',
         'bullets': [
             'Also called "pseudomembranous croup" or "bacterial croup"',
             'Often follows a viral URI/croup — then sudden deterioration with high fever and toxicity',
             'Key clue: child with croup-like symptoms who is TOXIC and NOT responding to standard croup treatment',
             'Pathogens: S. aureus (most common), S. pyogenes, M. catarrhalis, H. influenzae',
             'Thick purulent secretions and pseudomembranes in the trachea',
             'May require intubation for airway protection and pulmonary toilet',
             'Treatment: IV antibiotics (ceftriaxone + vancomycin or clindamycin), airway management',
             '>Think bacterial tracheitis when "croup + toxic + not responding to treatment"',
         ],
         'notes': 'Bacterial tracheitis is the diagnosis you make when croup treatment fails and the child looks sicker than expected. It requires a high index of suspicion. These children often need intubation — not for obstruction, but to manage the thick secretions and pseudomembranes. It is an ICU-level diagnosis.'},

        {'type': 'two_column', 'title': 'Disposition Decisions in Upper Airway Obstruction',
         'left_title': 'SAFE TO DISCHARGE',
         'left': [
             'Mild croup (Westley ≤2) after dexamethasone',
             'No stridor at rest after observation',
             'Tolerating PO fluids',
             'No epinephrine given, OR ≥2h post-epinephrine with no rebound',
             'Parents comfortable with return precautions',
             'Reliable follow-up available',
             'Late-night presentation that improved: observe until comfortable',
         ],
         'right_title': 'MUST ADMIT',
         'right': [
             'Stridor at rest persisting after treatment',
             'Multiple epinephrine doses required',
             'Severe croup (Westley ≥6)',
             'Any suspicion for epiglottitis or bacterial tracheitis',
             'Hypoxia requiring supplemental O2',
             'Inability to tolerate oral fluids',
             'Young infant (<6 months) with significant stridor',
             'Any concern for alternate diagnosis (FB, abscess)',
         ],
         'notes': 'Disposition is one of the most important decisions. The 2-hour observation period after nebulized epinephrine is non-negotiable — rebound can cause significant worsening. For mild croup discharged from the ED, give clear return precautions: worsening stridor, difficulty breathing, drooling, color change, decreased oral intake.'},

        {'type': 'pitfalls', 'title': 'Upper Airway Obstruction Pitfalls',
         'items': [
             'Forgetting dexamethasone for mild croup — ALL severity levels benefit',
             'Discharging too early after nebulized epinephrine (need ≥2h observation)',
             'Examining the oropharynx in suspected epiglottitis — can precipitate complete obstruction',
             'Assuming all stridor is croup — consider foreign body, abscess, anaphylaxis, hemangioma',
             'Not considering bacterial tracheitis when croup treatment fails',
             'Missing retropharyngeal abscess — neck stiffness + fever + drooling + refusal to extend neck',
             'Sending home a child with stridor at rest — this requires ongoing observation',
         ],
         'notes': 'The most commonly tested board pitfall: examining the throat in suspected epiglottitis. The most common clinical pitfall: not giving dexamethasone to mild croup because "they look fine." The most dangerous pitfall: assuming all stridor is benign viral croup and missing a surgical emergency.'},

        {'type': 'case', 'title': '2-Year-Old with Barky Cough at 2 AM',
         'scenario': 'A 2-year-old girl presents at 2 AM with barky cough and noisy breathing for 3 hours. Parents report 2 days of runny nose and low-grade fever (38.2°C). On exam: alert, intermittent barky cough, inspiratory stridor at rest, mild intercostal retractions, SpO2 97% on RA. No drooling. Westley Score: 4 (moderate croup).',
         'questions': [
             'Immediate management? (Dexamethasone 0.6 mg/kg PO + nebulized epinephrine)',
             '30 minutes later: stridor resolved at rest, happy, drinking juice — now what?',
             'Must observe ≥2 hours post-epinephrine before disposition decision',
             'At 2 hours: no stridor, playful, tolerating PO — safe to discharge with return precautions',
         ],
         'notes': 'Classic croup case. Teaching points: 1) Westley score of 4 = moderate = needs epinephrine in addition to dexamethasone. 2) The rapid response to epinephrine is expected — but you MUST wait for the observation period. 3) Discharge criteria: no stridor at rest, tolerating PO, no respiratory distress, adequate observation time post-epinephrine.'},
    ],
    'takeaways': [
        'ALL children with croup receive dexamethasone 0.6 mg/kg — even mild cases',
        'Observe at least 2 hours after nebulized epinephrine before discharge',
        'Epiglottitis = toxic + drooling + NO cough + rapid onset → do NOT examine throat, call for OR airway',
        'When croup treatment fails and child is toxic, think bacterial tracheitis',
        'Stridor at rest after treatment = admission; resolved stridor + tolerating PO = safe discharge',
    ],
    'references': [
        'Bjornson CL, Johnson DW. Croup in children. CMAJ. 2013;185(15):1317-1323.',
        'Russell KF, et al. Glucocorticoids for croup. Cochrane Database Syst Rev. 2011;1:CD001955.',
        'Gates A, et al. Glucocorticoids for croup in children. Cochrane Database Syst Rev. 2018;8:CD001955.',
        'Petrocheilou A, et al. Viral croup: diagnosis and a treatment algorithm. Pediatr Pulmonol. 2014;49(5):421-429.',
        'Shah RK, Roberson DW, Jones DT. Epiglottitis in the Hemophilus influenzae type B vaccine era. Laryngoscope. 2004;114(3):557-560.',
        'Hopkins A, et al. Changing epidemiology of life-threatening upper airway infections: the reemergence of bacterial tracheitis. Pediatrics. 2006;118(4):1418-1421.',
    ],
}

if __name__ == '__main__':
    build_deck(spec, os.path.join(os.path.dirname(__file__), '08_croup_epiglottitis_upper_airway.pptx'))

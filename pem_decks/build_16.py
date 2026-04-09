#!/usr/bin/env python3
"""Build Deck 16: DKA in Children"""
import sys, os
sys.path.insert(0, os.path.dirname(__file__))
from build_deck import build_deck

spec = {
    'number': 16,
    'short_title': 'Pediatric DKA',
    'title': 'DKA in Children: Emergency Recognition and Management',
    'subtitle': 'Fluid Resuscitation, Insulin Therapy, Electrolyte Management, and Cerebral Edema Prevention',
    'objectives': [
        'Recognize DKA presentation including subtle and new-onset diabetes presentations',
        'Classify DKA severity (mild, moderate, severe) and initiate appropriate management',
        'Execute evidence-based fluid and insulin protocols with cerebral edema precautions',
        'Monitor and replace electrolytes — especially potassium and phosphorus',
        'Recognize and emergently treat cerebral edema — the deadliest DKA complication',
    ],
    'slides': [
        {'type': 'content', 'title': 'DKA in Pediatric Emergency Medicine',
         'bullets': [
             'DKA occurs in ~30% of children at initial diabetes diagnosis (higher in younger children)',
             'Diagnostic criteria: glucose >200, pH <7.3 or HCO3 <15, ketonemia/ketonuria',
             'Most common precipitant in known diabetics: missed insulin doses, infection, pump failure',
             'Mortality: 0.15-0.3% in developed countries — cerebral edema accounts for 60-90% of deaths',
             'Young children (<5 years) and new-onset diabetes are highest risk for cerebral edema',
             '>DKA is the leading cause of death in children with type 1 diabetes',
         ],
         'notes': 'Frame DKA as a true emergency. While mortality is low, it is NOT zero, and the consequences of cerebral edema are devastating. The most commonly missed DKA presentations: the young child with "gastroenteritis" (vomiting, abdominal pain, dehydration) who actually has new-onset diabetes. Always check a glucose in a dehydrated, vomiting child.'},

        {'type': 'table', 'title': 'DKA Severity Classification',
         'headers': ['Parameter', 'Mild', 'Moderate', 'Severe'],
         'rows': [
             ['pH', '7.2-7.3', '7.1-7.2', '<7.1'],
             ['Bicarbonate', '10-15 mEq/L', '5-10 mEq/L', '<5 mEq/L'],
             ['Mental Status', 'Alert', 'Alert/drowsy', 'Obtunded/coma'],
             ['Dehydration', '5%', '7%', '10%'],
             ['Kussmaul Breathing', 'Mild', 'Moderate', 'Severe, deep'],
             ['Management Setting', 'ED/floor', 'Floor/stepdown', 'PICU'],
         ],
         'notes': 'Severity classification guides your management intensity and disposition. Severe DKA (pH <7.1) always requires PICU admission. The mental status component is critical — any alteration in consciousness in DKA raises concern for cerebral edema. Young children with new-onset DKA frequently present with severe disease because the diagnosis was delayed.'},

        {'type': 'algorithm', 'title': 'DKA Management Protocol',
         'steps': [
             'HOUR 0: ABCs, 2 IVs, labs (BMP, VBG, CBC, UA, beta-hydroxybutyrate), 10-20 mL/kg NS bolus (if clinically dehydrated)',
             'HOUR 0-1: Start 2-bag system: D10 NS + 40 KCl/bag and NS + 40 KCl/bag; run at 1.5x maintenance',
             'HOUR 1: Start insulin drip 0.05-0.1 units/kg/hr (NO bolus); continue fluids',
             'HOURS 1-24: Monitor BG hourly, BMP q2h, neuro checks q1h; titrate D10 to keep glucose 150-300',
             'TRANSITION: When pH >7.3, HCO3 >15, AG closed, tolerating PO → overlap SQ insulin 30 min before stopping drip',
             'Throughout: neuro checks hourly — any deterioration → STOP fluids, hypertonic saline, call PICU',
         ],
         'notes': 'This is the core protocol. Key changes from older protocols: lower fluid rates (1.5x maintenance, not 2x), insulin dose 0.05-0.1 (not always 0.1), NO insulin bolus (increases cerebral edema risk). The 2-bag system allows you to titrate dextrose concentration without changing the fluid rate. Keep glucose 150-300 during treatment — do NOT try to normalize glucose rapidly.'},

        {'type': 'key_point', 'title': 'PECARN FLUID Trial',
         'point': 'The rate and type of IV fluid in pediatric DKA does NOT affect neurologic outcomes.',
         'sub': 'Kuppermann et al., NEJM 2018. 1,389 children randomized to 4 fluid strategies. No difference in neurocognitive outcomes at 6 months. However, rapid correction of glucose and osmolality should still be avoided. Current standard: NS at 1.5x maintenance after initial bolus.',
         'notes': 'The PECARN FLUID trial was the definitive study addressing decades of controversy about fluid resuscitation in DKA. The bottom line: neither fast vs. slow fluids nor 0.9% vs. 0.45% saline affected outcomes. This does NOT mean fluids do not matter — it means that within the studied ranges, the approach is safe. Standard practice remains measured, not aggressive, fluid replacement.'},

        {'type': 'table', 'title': 'Electrolyte Management in DKA',
         'headers': ['Electrolyte', 'Issue in DKA', 'Replacement', 'Monitoring'],
         'rows': [
             ['Potassium', 'Total body depleted despite normal/high serum K', 'Add 40 mEq KCl/L to all fluids once K <5.5 and urine output confirmed', 'q2h BMP; watch for hypokalemia with insulin'],
             ['Sodium', 'Factitious hyponatremia from hyperglycemia', 'Corrected Na = measured Na + 1.6 × [(glucose-100)/100]', 'Corrected Na should RISE with treatment; if falling → concern for CE'],
             ['Phosphorus', 'Total body depleted', 'Consider KPhos if <1.5 mg/dL', 'Risk of hypocalcemia with replacement'],
             ['Bicarbonate', 'Low due to ketoacidosis', 'Do NOT give IV bicarbonate — increases CE risk', 'Normalizes with insulin + fluids'],
         ],
         'notes': 'Potassium is the most critical electrolyte to manage. Insulin drives K into cells — a patient can become dangerously hypokalemic within hours of starting insulin. If K is <3.5, hold insulin until K is repleted. The corrected sodium is essential for monitoring: it should rise as glucose falls. A FALLING corrected sodium during treatment is the earliest laboratory sign of impending cerebral edema.'},

        {'type': 'content', 'title': 'Cerebral Edema: The Deadliest Complication',
         'bullets': [
             'Occurs in 0.5-1% of pediatric DKA episodes — 20-25% mortality, 35% permanent neurologic injury',
             'Usually occurs 4-12 hours into treatment (but can occur before or after)',
             'Risk factors: younger age, new-onset diabetes, severe DKA, high BUN, failure of corrected Na to rise',
             'Clinical signs: headache, vomiting, altered mental status, hypertension, bradycardia, pupil changes',
             'Cushing triad (HTN + bradycardia + irregular breathing) = late and ominous sign',
             'TREATMENT IS EMERGENT:',
             '>3% hypertonic saline 5 mL/kg IV over 10-15 min OR mannitol 0.5-1 g/kg IV',
             '>Elevate HOB 30°, reduce IV fluids to minimum, intubate if needed (avoid hyperventilation)',
         ],
         'notes': 'Cerebral edema in DKA is the emergency within the emergency. The key to saving these children is EARLY recognition. Do not wait for Cushing triad — that is a late sign indicating herniation. Headache and altered mental status during DKA treatment should trigger immediate action. Have hypertonic saline prepared and at the bedside for every moderate-severe DKA patient.'},

        {'type': 'two_column', 'title': 'DKA Mimics and Diagnostic Traps',
         'left_title': 'DKA MIMICS',
         'left': [
             'Gastroenteritis (vomiting, abdominal pain, dehydration)',
             'Appendicitis (abdominal pain, leukocytosis)',
             'Pneumonia (Kussmaul breathing misread as respiratory distress)',
             'Sepsis (tachycardia, dehydration, acidosis)',
             'Asthma (deep breathing, tachycardia)',
             'UTI (polyuria, incontinence in new-onset DM)',
         ],
         'right_title': 'AVOID THESE TRAPS',
         'right': [
             'Always check glucose in a dehydrated, vomiting child',
             'Do not give IV bicarbonate — it worsens cerebral edema',
             'Do not bolus insulin — increases cerebral edema risk',
             'Do not drop glucose faster than 50-100 mg/dL/hr',
             'Do not forget potassium replacement',
             'Do not discharge until anion gap is closed (not just until glucose normalizes)',
         ],
         'notes': 'The DKA mimics are critical teaching material. The most commonly missed DKA presentation is the young child diagnosed with "viral gastroenteritis" who actually has new-onset diabetes. Every child with vomiting and dehydration should have a glucose checked. The right column contains the most dangerous management errors.'},

        {'type': 'case', 'title': '4-Year-Old with Vomiting and Lethargy',
         'scenario': 'A 4-year-old girl is brought in for 3 days of vomiting, decreased oral intake, and increasing sleepiness. Parents thought it was a stomach bug. She has been urinating more than usual for the past 2 weeks. On exam: lethargic, dry mucous membranes, Kussmaul respirations, fruity breath, HR 145, BP 95/60, RR 36, SpO2 99%. Labs: glucose 487, pH 7.05, HCO3 4, K 5.8, Na 128, BUN 32.',
         'questions': [
             'Severity classification? (Severe DKA — pH <7.1, HCO3 <5, altered mental status)',
             'Corrected sodium? Na 128 + 1.6 × [(487-100)/100] = 128 + 6.2 = 134.2 (normal corrected)',
             'Initial management? (NS 10 mL/kg bolus, then 2-bag system at 1.5x maintenance with K)',
             'When to start insulin? (After first bolus, once K confirmed <6.5 → insulin 0.05 units/kg/hr)',
             'Disposition? (PICU — severe DKA, AMS, age <5, new-onset diabetes, all high-risk features for CE)',
         ],
         'notes': 'This case has every high-risk feature for cerebral edema: young age, new-onset diabetes, severe DKA, high BUN. This child needs PICU admission and hourly neuro checks. Walk through the corrected sodium calculation — this is a board favorite and a clinical necessity. Note that the K of 5.8 is actually falsely elevated due to acidosis — do NOT give extra K yet, but start it soon.'},

        {'type': 'pitfalls', 'title': 'DKA Management Pitfalls',
         'items': [
             'Missing new-onset diabetes presenting as "gastroenteritis" — check glucose in dehydrated, vomiting children',
             'Giving an insulin bolus — this is contraindicated and increases cerebral edema risk',
             'Giving IV bicarbonate — also increases cerebral edema risk; acidosis resolves with insulin + fluids',
             'Dropping glucose too rapidly — aim for 50-100 mg/dL/hr; use 2-bag system with D10',
             'Forgetting potassium replacement — insulin drives K intracellularly; hypokalemia can be fatal',
             'Stopping insulin when glucose normalizes — keep insulin running until anion gap closes; add dextrose',
             'Not monitoring corrected sodium — falling corrected Na = cerebral edema warning sign',
         ],
         'notes': 'Every pitfall on this list has killed children. The insulin bolus prohibition is absolute. The bicarbonate prohibition is equally absolute in pediatric DKA. Stopping insulin based on glucose normalization (rather than gap closure) leads to rebound ketoacidosis. And the corrected sodium is your early warning system for cerebral edema.'},
    ],
    'takeaways': [
        'Always check glucose in a vomiting, dehydrated child — DKA mimics gastroenteritis',
        'NO insulin bolus, NO IV bicarbonate in pediatric DKA — both increase cerebral edema risk',
        'Use the 2-bag system: titrate dextrose to keep glucose 150-300 while insulin runs',
        'Monitor corrected sodium — a falling corrected Na during treatment is the earliest sign of cerebral edema',
        'Cerebral edema treatment: 3% hypertonic saline 5 mL/kg IV IMMEDIATELY if suspected',
    ],
    'references': [
        'Wolfsdorf JI, et al. ISPAD Clinical Practice Consensus Guidelines 2018: Diabetic ketoacidosis and the hyperglycemic hyperosmolar state. Pediatr Diabetes. 2018;19(Suppl 27):155-177.',
        'Kuppermann N, et al. Clinical trial of fluid infusion rates for pediatric diabetic ketoacidosis (PECARN FLUID). N Engl J Med. 2018;378(24):2275-2287.',
        'Glaser N, et al. Risk factors for cerebral edema in children with diabetic ketoacidosis. N Engl J Med. 2001;344(4):264-269.',
        'DePiero A, et al. Performance of the PECARN DKA FLUID trial fluid protocol outside a clinical trial. Pediatr Emerg Care. 2021;37(12):e1523-e1528.',
        'Tasker RC, et al. Cerebral edema in children with diabetic ketoacidosis: vasogenic rather than cellular? Pediatr Diabetes. 2005;6(2):75-78.',
    ],
}

if __name__ == '__main__':
    build_deck(spec, os.path.join(os.path.dirname(__file__), '16_dka_children.pptx'))

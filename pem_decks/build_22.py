#!/usr/bin/env python3
"""Build Deck 22: Anaphylaxis and Allergic Emergencies"""
import sys, os
sys.path.insert(0, os.path.dirname(__file__))
from build_deck import build_deck

spec = {
    'number': 22,
    'short_title': 'Anaphylaxis',
    'title': 'Anaphylaxis and Allergic Emergencies',
    'subtitle': 'Rapid Recognition, Epinephrine-First Approach, and Biphasic Reaction Management',
    'objectives': [
        'Recognize anaphylaxis using NIAID/FAAN diagnostic criteria — not just "classic" presentations',
        'Administer IM epinephrine immediately — the only first-line treatment for anaphylaxis',
        'Manage refractory anaphylaxis with IV epinephrine, fluids, and vasopressors',
        'Understand the risk of biphasic reactions and apply appropriate observation periods',
        'Differentiate anaphylaxis from common mimics in children',
    ],
    'slides': [
        {'type': 'content', 'title': 'Anaphylaxis: The Most Time-Critical Allergic Emergency',
         'bullets': [
             'Anaphylaxis = a severe, potentially fatal, systemic allergic reaction',
             'Incidence in children: rising — estimated 1-2% lifetime prevalence',
             'Most common triggers in children: food (peanuts, tree nuts, milk, egg), insect stings, medications',
             'Death from anaphylaxis is almost always from DELAYED or WITHHELD epinephrine',
             'Epinephrine is the ONLY first-line treatment — antihistamines and steroids are adjuncts, not replacements',
             '>The biggest error in anaphylaxis management: not giving epinephrine early enough',
         ],
         'notes': 'The central message: epinephrine early saves lives. Studies consistently show that the primary risk factor for fatal anaphylaxis is delayed administration of epinephrine. Antihistamines treat symptoms (urticaria, pruritus) but do NOT reverse airway edema, bronchospasm, or hypotension. Steroids may reduce biphasic reactions but have no role in acute treatment.'},

        {'type': 'table', 'title': 'NIAID/FAAN Diagnostic Criteria for Anaphylaxis',
         'headers': ['Criterion', 'Details', 'Examples'],
         'rows': [
             ['Criterion 1', 'Skin/mucosal involvement + respiratory compromise OR hypotension', 'Urticaria + wheezing; angioedema + stridor; hives + syncope'],
             ['Criterion 2', '≥2 organ systems after LIKELY allergen exposure: skin, respiratory, GI, cardiovascular', 'Hives + vomiting; lip swelling + wheeze; flushing + abdominal pain'],
             ['Criterion 3', 'Hypotension after exposure to KNOWN allergen', 'Peanut-allergic child develops hypotension after peanut exposure'],
         ],
         'notes': 'Any ONE of these three criteria = anaphylaxis. Key point: skin findings are NOT required. Up to 20% of anaphylaxis presents WITHOUT skin involvement. Isolated GI symptoms (vomiting, abdominal pain) after allergen exposure with one other organ system = anaphylaxis. Do not wait for the "classic" presentation of hives + wheeze + hypotension — give epinephrine based on these criteria.'},

        {'type': 'algorithm', 'title': 'Anaphylaxis Management Protocol',
         'steps': [
             'STEP 1: EPINEPHRINE IM 0.01 mg/kg (1:1000; max 0.3 mg child, 0.5 mg adult) — anterolateral thigh',
             'STEP 2: Position — supine with legs elevated (unless vomiting or in respiratory distress → position of comfort)',
             'STEP 3: High-flow O2, IV access × 2, cardiac monitor, prepare for airway management',
             'STEP 4: NS 20 mL/kg IV bolus for hypotension — repeat as needed',
             'STEP 5: Repeat IM epinephrine every 5-15 minutes if symptoms not improving (max 3 doses)',
             'STEP 6: Adjuncts — diphenhydramine 1 mg/kg IV, ranitidine 1 mg/kg IV, dexamethasone 0.6 mg/kg IV',
             'STEP 7: If refractory → IV epinephrine infusion 0.1-1 mcg/kg/min; consider glucagon if on beta-blockers',
         ],
         'notes': 'The algorithm is epinephrine-centered. Antihistamines and steroids are step 6, not step 1. The IM route (anterolateral thigh) provides faster absorption than subcutaneous. Repeat dosing every 5-15 minutes is safe and necessary for refractory anaphylaxis. If three IM doses fail, transition to IV epinephrine infusion with close hemodynamic monitoring.'},

        {'type': 'key_point', 'title': 'Clinical Pearl',
         'point': 'There is NO contraindication to epinephrine in anaphylaxis.',
         'sub': 'Not age, not cardiac disease, not beta-blocker use, not pregnancy. The risk of NOT giving epinephrine always exceeds the risk of giving it. The dose is 0.01 mg/kg IM (1:1000) to the anterolateral thigh. If you diagnose anaphylaxis, give epinephrine.',
         'notes': 'This pearl addresses the most common barrier to epinephrine administration: fear. Physicians and nurses hesitate to give epinephrine because of concerns about cardiac side effects. But epinephrine at the IM dose of 0.01 mg/kg is remarkably safe. The risk of withholding it in true anaphylaxis is death. Give it.'},

        {'type': 'two_column', 'title': 'Anaphylaxis vs. Common Mimics',
         'left_title': 'ANAPHYLAXIS (Give Epinephrine)',
         'left': [
             'Rapid onset after allergen exposure',
             'Multiple organ systems involved',
             'Urticaria/angioedema + respiratory or cardiovascular symptoms',
             'Stridor, hoarseness, tongue/lip swelling',
             'Wheezing, dyspnea, bronchospasm',
             'Hypotension, tachycardia, syncope',
             'Abdominal pain, vomiting with other symptoms',
         ],
         'right_title': 'NOT ANAPHYLAXIS (Different Treatment)',
         'right': [
             'Acute urticaria alone (no respiratory/CV involvement) → antihistamines',
             'Vasovagal syncope (bradycardia, pallor, no urticaria) → supine, IV fluids',
             'Panic attack (hyperventilation, tingling, no urticaria) → reassurance',
             'Asthma exacerbation (wheezing without skin/GI/CV) → albuterol/steroids',
             'Hereditary angioedema (angioedema without urticaria) → C1-INH concentrate',
             'Scombroid (histamine fish poisoning) → antihistamines',
         ],
         'notes': 'The key distinction: anaphylaxis involves MULTIPLE organ systems after allergen exposure. Isolated urticaria (even severe) is NOT anaphylaxis and does not require epinephrine. Vasovagal syncope is the most common mimic — differentiated by bradycardia (vs. tachycardia in anaphylaxis) and lack of urticaria/angioedema.'},

        {'type': 'content', 'title': 'Biphasic Anaphylaxis and Observation Period',
         'bullets': [
             'Biphasic reaction: recurrence of symptoms 1-72 hours after initial resolution (without re-exposure)',
             'Incidence: 1-20% depending on definition and study',
             'Risk factors: severe initial reaction, delayed epinephrine, prior biphasic reaction',
             'Most biphasic reactions occur within 8-12 hours of initial reaction',
             'Recommended observation: minimum 4-6 hours for mild-moderate anaphylaxis; 12-24 hours for severe',
             'Discharge with: EpiPen prescription, allergy referral, anaphylaxis action plan, return precautions',
             '>Every patient who receives epinephrine for anaphylaxis should be observed for at least 4 hours',
         ],
         'notes': 'The biphasic reaction risk drives the observation period. The evidence base for the optimal observation time is limited, but most experts recommend 4-6 hours minimum. Severe anaphylaxis (requiring multiple epinephrine doses, intubation, or vasopressors) warrants longer observation or admission. Every patient discharged after anaphylaxis needs an EpiPen prescription and an action plan.'},

        {'type': 'case', 'title': '5-Year-Old with Facial Swelling After Eating Cashews',
         'scenario': 'A 5-year-old girl (20 kg) with known peanut allergy ate a cookie at a birthday party. Within 10 minutes she developed lip swelling, hives on her trunk, and then started coughing and wheezing. Parent gave diphenhydramine at home and brought her to the ED. On arrival: anxious, diffuse urticaria, audible wheeze, lip and tongue edema, SpO2 94%, HR 140, BP 80/50.',
         'questions': [
             'Is this anaphylaxis? (YES — skin + respiratory + cardiovascular involvement, known allergen exposure)',
             'First treatment? (Epinephrine 0.01 mg/kg = 0.2 mg IM to anterolateral thigh — NOT diphenhydramine)',
             'Additional? (O2, NS 20 mL/kg bolus for hypotension, albuterol for wheezing, repeat epi in 5-15 min if no improvement)',
             'Parent gave only diphenhydramine — what is the teaching point? (Epinephrine first, always; antihistamines do not treat anaphylaxis)',
         ],
         'notes': 'This case demonstrates the most common error: treating anaphylaxis with antihistamines first. The parent did what many parents and some physicians do — reach for diphenhydramine. But diphenhydramine does not treat bronchospasm, laryngeal edema, or hypotension. Epinephrine does. Use this case to reinforce the epinephrine-first message.'},

        {'type': 'pitfalls', 'title': 'Anaphylaxis Pitfalls',
         'items': [
             'Giving antihistamines instead of epinephrine as first treatment — epinephrine is ALWAYS first',
             'Using subcutaneous instead of IM route — IM anterolateral thigh provides faster, higher peak levels',
             'Not repeating epinephrine when symptoms persist — can repeat every 5-15 minutes, up to 3 IM doses',
             'Discharging without EpiPen prescription — every anaphylaxis patient needs an auto-injector at discharge',
             'Too-short observation period — minimum 4 hours; longer for severe reactions',
             'Missing anaphylaxis without skin findings — 20% of anaphylaxis has NO urticaria or angioedema',
             'Not referring to allergy/immunology — these patients need formal evaluation and an anaphylaxis action plan',
         ],
         'notes': 'Every one of these pitfalls is a common, documented error. The subcutaneous route was previously taught but provides slower and lower drug levels than IM. The EpiPen prescription at discharge is medico-legally and clinically essential — it allows treatment of future episodes before reaching the ED.'},
    ],
    'takeaways': [
        'Epinephrine IM (0.01 mg/kg, max 0.3 mg child) is the ONLY first-line treatment for anaphylaxis',
        'There is NO contraindication to epinephrine in anaphylaxis — the risk of withholding it is death',
        'Antihistamines and steroids are adjuncts, NOT replacements for epinephrine',
        'Observe minimum 4-6 hours after anaphylaxis; discharge with EpiPen + allergy referral + action plan',
        'Up to 20% of anaphylaxis presents WITHOUT skin findings — use NIAID criteria, not "classic" presentation',
    ],
    'references': [
        'Sampson HA, et al. Second symposium on the definition and management of anaphylaxis: summary report. J Allergy Clin Immunol. 2006;117(2):391-397.',
        'Simons FER, et al. World Allergy Organization guidelines for the assessment and management of anaphylaxis. J Allergy Clin Immunol. 2011;127(3):587-593.',
        'Shaker MS, et al. Anaphylaxis — a 2020 practice parameter update, systematic review, and GRADE analysis. J Allergy Clin Immunol. 2020;145(4):1082-1123.',
        'Lee S, et al. Time of onset and predictors of biphasic anaphylactic reactions: a systematic review and meta-analysis. J Allergy Clin Immunol Pract. 2015;3(3):408-416.',
        'Campbell RL, et al. Evaluation of a predictive model for anaphylaxis in the ED. Am J Emerg Med. 2012;30(8):1204-1209.',
    ],
}

if __name__ == '__main__':
    build_deck(spec, os.path.join(os.path.dirname(__file__), '22_anaphylaxis.pptx'))

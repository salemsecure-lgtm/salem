#!/usr/bin/env python3
"""Build Deck 21: Toxicology and Common Pediatric Poisonings"""
import sys, os
sys.path.insert(0, os.path.dirname(__file__))
from build_deck import build_deck

spec = {
    'number': 21,
    'short_title': 'Pediatric Toxicology',
    'title': 'Toxicology and Common Pediatric Poisonings',
    'subtitle': 'Toxidromes, Lethal Ingestions, Decontamination, and Antidote Therapy',
    'objectives': [
        'Recognize the 5 classic toxidromes and their clinical features',
        'Identify the "one pill can kill" medications in toddlers',
        'Execute GI decontamination decisions: when activated charcoal is and is not appropriate',
        'Know key antidotes and their dosing for the most common pediatric poisonings',
        'Manage acetaminophen, salicylate, and iron poisoning — the most common serious ingestions',
    ],
    'slides': [
        {'type': 'content', 'title': 'Pediatric Poisoning: The PEM Landscape',
         'bullets': [
             'Poisoning accounts for >1 million calls to US Poison Control for children <6 years annually',
             'Peak age: 1-3 years (exploratory ingestions) — "the hand-to-mouth phase"',
             'Second peak: adolescents (intentional ingestions — suicide attempts, recreational)',
             'Most pediatric ingestions are non-toxic or minimally toxic — but some are lethal',
             'The PEM role: risk-stratify rapidly, identify the dangerous ingestions, and treat aggressively',
             '>Call Poison Control (1-800-222-1222) early — they are an invaluable resource for real-time guidance',
         ],
         'notes': 'Frame the talk around risk stratification. Most calls to poison control for children involve non-toxic exposures. But the subset of dangerous ingestions requires rapid identification and treatment. Poison Control should be your partner in every toxicology case — call them early.'},

        {'type': 'table', 'title': 'The 5 Classic Toxidromes',
         'headers': ['Toxidrome', 'Vitals', 'Pupils', 'Skin', 'Other', 'Common Agents'],
         'rows': [
             ['Anticholinergic', '↑HR, ↑T, ↑BP', 'Dilated (mydriasis)', 'Dry, flushed, hot', 'Delirium, urinary retention, ileus', 'Diphenhydramine, atropine, jimsonweed, TCAs'],
             ['Cholinergic', '↓HR (or ↑), ↓BP', 'Constricted (miosis)', 'Diaphoretic', 'SLUDGE/DUMBELS', 'Organophosphates, carbamates, nerve agents'],
             ['Sympathomimetic', '↑HR, ↑BP, ↑T', 'Dilated', 'Diaphoretic', 'Agitation, seizures', 'Cocaine, amphetamines, pseudoephedrine'],
             ['Opioid', '↓HR, ↓BP, ↓RR', 'Pinpoint', 'Normal/cool', 'CNS depression, apnea', 'Morphine, fentanyl, heroin, methadone'],
             ['Sedative-Hypnotic', '↓HR, ↓BP, ↓RR', 'Variable', 'Normal', 'CNS depression, ataxia', 'Benzodiazepines, barbiturates, ethanol'],
         ],
         'notes': 'These 5 toxidromes must be memorized. The key distinguishing features: anticholinergic = DRY + hot + dilated pupils + delirium. Cholinergic = WET + everything leaking (SLUDGE). Sympathomimetic = hot + diaphoretic + agitated + dilated. Opioid = pinpoint pupils + respiratory depression. Sedative-hypnotic = CNS depression without specific pupil or autonomic changes.'},

        {'type': 'content', 'title': '"One Pill Can Kill" — Lethal Ingestions in Toddlers',
         'bullets': [
             'These medications can be fatal with a single pill/dose in a small child:',
             '>Calcium channel blockers (especially verapamil, diltiazem): hypotension, bradycardia, cardiac arrest',
             '>Beta-blockers (especially propranolol): bradycardia, hypotension, seizures, hypoglycemia',
             '>Sulfonylureas (glyburide, glipizide): profound, prolonged hypoglycemia lasting 24-72 hours',
             '>Opioids (methadone, buprenorphine patches): respiratory depression, apnea',
             '>Tricyclic antidepressants: seizures, wide QRS, cardiac arrest',
             '>Clonidine: bradycardia, hypotension, CNS depression, apnea',
             '>Camphor, chloroquine, theophylline: seizures, cardiac toxicity',
             'These children need prolonged observation (6-24 hours minimum) even if asymptomatic at presentation',
         ],
         'notes': 'This list must be memorized for boards and for practice. The calcium channel blocker and sulfonylurea ingestions are the most dangerous because they can be completely asymptomatic initially and then cause sudden cardiovascular collapse or hypoglycemia hours later. Extended observation is mandatory.'},

        {'type': 'algorithm', 'title': 'GI Decontamination Decision Framework',
         'steps': [
             'ACTIVATED CHARCOAL (AC): Consider if toxic ingestion <1-2 hours ago AND substance binds to charcoal AND airway is protected',
             'AC dose: 1 g/kg (max 50g) PO or via NG tube; mix with cola or juice for palatability',
             'AC does NOT bind: iron, lithium, alcohols, acids/alkalis, potassium, heavy metals',
             'WHOLE BOWEL IRRIGATION: For sustained-release pills, iron, body packers — GoLYTELY 500 mL/hr (child) or 1-2 L/hr (adolescent)',
             'GASTRIC LAVAGE: Rarely indicated; only for massive life-threatening ingestion <1 hour ago',
             'SYRUP OF IPECAC: NEVER — no longer recommended in any setting',
         ],
         'notes': 'The main message: activated charcoal is useful but has a narrow window (best within 1 hour). Most children present to the ED well after this window, making charcoal less effective. It is contraindicated if the airway is not protected or if there is a risk of aspiration. Whole bowel irrigation is reserved for specific situations.'},

        {'type': 'table', 'title': 'Key Antidotes Quick Reference',
         'headers': ['Poison', 'Antidote', 'Dose', 'Key Notes'],
         'rows': [
             ['Acetaminophen', 'N-acetylcysteine (NAC)', 'PO: 140 mg/kg load then 70 mg/kg q4h × 17; IV: 150→50→100 mg/kg', 'Start within 8h if possible; use Rumack-Matthew nomogram'],
             ['Opioids', 'Naloxone', '0.1 mg/kg IV/IM/IN (max 2 mg); repeat q2-3 min', 'Short half-life — may need repeat doses or infusion'],
             ['Benzodiazepines', 'Flumazenil', '0.01 mg/kg IV (max 0.2 mg); repeat q1 min', 'CAUTION in chronic benzo use or TCA co-ingestion (seizure risk)'],
             ['Organophosphates', 'Atropine + Pralidoxime', 'Atropine 0.05 mg/kg IV; 2-PAM 25-50 mg/kg IV', 'Atropine until drying of secretions; pralidoxime within 24h'],
             ['Iron', 'Deferoxamine', '15 mg/kg/hr IV continuous (max 6g/day)', 'Indicated for peak iron >500, shock, metabolic acidosis'],
             ['TCAs', 'Sodium bicarbonate', '1-2 mEq/kg IV bolus', 'For QRS >100ms; target serum pH 7.45-7.55'],
             ['Beta-blockers', 'Glucagon', '0.05-0.1 mg/kg IV (max 5 mg)', 'Followed by infusion; also high-dose insulin therapy'],
             ['Ca-channel blockers', 'Calcium + high-dose insulin', 'CaCl2 20 mg/kg IV; Insulin 1 U/kg bolus → 1 U/kg/hr', 'High-dose insulin-euglycemia therapy is first-line for CCB'],
         ],
         'notes': 'This antidote table should be posted in your resus bay. Key points: NAC is most effective within 8 hours but still beneficial up to 24 hours after acetaminophen ingestion. Naloxone has a shorter half-life than most opioids — plan for re-dosing or infusion. Sodium bicarbonate for TCAs targets the QRS width, not the pH. High-dose insulin therapy for CCB poisoning is the most important recent advance.'},

        {'type': 'content', 'title': 'Acetaminophen: The Most Common Serious Ingestion',
         'bullets': [
             'Most common cause of acute liver failure in the US (pediatric and adult)',
             'Toxic dose: >150 mg/kg in a single ingestion',
             'Phases: I (0-24h: nausea, vomiting), II (24-72h: clinical improvement but rising LFTs), III (72-96h: hepatic failure), IV (recovery or death)',
             'Rumack-Matthew nomogram: plot APAP level at 4+ hours post-ingestion against time — determines need for NAC',
             'NAC protocol (IV): 150 mg/kg over 1h → 50 mg/kg over 4h → 100 mg/kg over 16h',
             'Start NAC within 8 hours for best outcomes — but it is STILL beneficial up to 24+ hours',
             '>Get a 4-hour APAP level for ALL intentional ingestions and any child with possible acetaminophen exposure',
         ],
         'notes': 'Acetaminophen toxicity is insidious because the child may look well in Phase I and Phase II while liver damage is progressing. The 4-hour level and nomogram are the decision tools. If the level plots above the treatment line, start NAC. If the time of ingestion is unknown, start NAC and get the level. In adolescent intentional overdoses, always check an APAP level regardless of what they say they took.'},

        {'type': 'two_column', 'title': 'Intentional vs. Accidental Ingestion Approach',
         'left_title': 'ACCIDENTAL (Toddler)',
         'left': [
             'Usually single substance',
             'Small amount (exploratory taste)',
             'Call Poison Control for risk assessment',
             'Many are non-toxic — observe and discharge',
             'Exception: "one pill can kill" drugs — always observe 6-24h',
             'Focus: identify the substance, estimate amount, observe',
         ],
         'right_title': 'INTENTIONAL (Adolescent)',
         'right': [
             'Often polysubstance ingestion — assume the worst',
             'May minimize or lie about what was taken',
             'Always check: APAP level, salicylate level, EKG, BMP',
             'Always get a urine drug screen',
             'Psychiatric evaluation mandatory before discharge',
             'Focus: stabilize, rule out dangerous co-ingestions, psych consult',
         ],
         'notes': 'The approach is completely different based on intent. The toddler who tasted something gets a Poison Control call and observation. The adolescent who took a handful of pills gets an aggressive workup including levels that do not depend on history (APAP, ASA) plus an EKG. APAP and salicylate levels are mandatory in ALL intentional ingestions because patients often do not accurately report what they took.'},

        {'type': 'case', 'title': '2-Year-Old Found with Grandmother\'s Pill Bottle',
         'scenario': 'A 2-year-old boy (12 kg) is brought in after his mother found him playing with his grandmother\'s glyburide (5 mg pills). She thinks he may have swallowed 1-2 pills. He looks well, is playful, and vital signs are normal. Point-of-care glucose is 85 mg/dL.',
         'questions': [
             'Is this a "one pill can kill" scenario? (YES — sulfonylureas cause prolonged hypoglycemia)',
             'Current glucose is normal — safe to discharge? (ABSOLUTELY NOT — hypoglycemia may be delayed 2-8 hours)',
             'Management? (Dextrose-containing IV fluids, glucose monitoring q1h for 24 hours, admission)',
             'If glucose drops <60: D10W 5 mL/kg IV bolus; consider octreotide 1-2 mcg/kg SC q6h',
             'This child needs 24-hour admission for glucose monitoring even if he never becomes hypoglycemic',
         ],
         'notes': 'This is the classic boards question. Sulfonylurea ingestion in a toddler can cause delayed, prolonged, and recurrent hypoglycemia lasting up to 72 hours. A normal glucose at presentation does NOT rule out danger. These children must be admitted for glucose monitoring. Octreotide (somatostatin analog) inhibits insulin release and is the specific treatment for sulfonylurea-induced hypoglycemia.'},

        {'type': 'pitfalls', 'title': 'Pediatric Toxicology Pitfalls',
         'items': [
             'Discharging a toddler with a normal glucose after sulfonylurea ingestion — hypoglycemia can be delayed hours',
             'Not checking APAP and ASA levels in intentional adolescent ingestions — always check regardless of history',
             'Giving flumazenil to a patient who chronically uses benzodiazepines or co-ingested TCAs → seizures',
             'Missing co-ingestions — adolescents rarely take just one thing; always assume polysubstance',
             'Relying on urine drug screens — they miss many dangerous substances and have poor sensitivity',
             'Forgetting to get an EKG in any unknown or intentional ingestion — QRS and QTc guide management',
             'Not calling Poison Control — they provide expert, real-time guidance specific to the exposure',
         ],
         'notes': 'Every pitfall on this list has led to patient harm. The sulfonylurea discharge is the most commonly tested. The APAP/ASA level in intentional ingestion is the most commonly missed step. The flumazenil warning is critical: in TCA co-ingestion, flumazenil removes benzodiazepine protection against TCA-induced seizures, which can be fatal.'},
    ],
    'takeaways': [
        'Know the 5 toxidromes cold — they guide your differential and treatment before labs return',
        '"One pill can kill" in toddlers: CCBs, beta-blockers, sulfonylureas, opioids, TCAs, clonidine',
        'Check APAP level, salicylate level, and EKG in ALL intentional ingestions regardless of reported history',
        'Sulfonylurea ingestion requires 24-hour glucose monitoring and admission even if initially normoglycemic',
        'Call Poison Control (1-800-222-1222) early and often — they are your real-time toxicology consultants',
    ],
    'references': [
        'Mowry JB, et al. 2021 Annual Report of the National Poison Data System (NPDS). Clin Toxicol. 2022;60(12):1381-1643.',
        'Liebelt EL, et al. Clinical predictors of insulin response in sulfonylurea poisoning. J Toxicol Clin Toxicol. 2000;38(2):127-133.',
        'Brok J, et al. Interventions for paracetamol (acetaminophen) overdose. Cochrane Database Syst Rev. 2006;2:CD003328.',
        'Hoffman RS, et al. Goldfrank\'s Toxicologic Emergencies. 11th ed. McGraw-Hill; 2019.',
        'American Academy of Clinical Toxicology/European Association of Poisons Centres. Position paper: single-dose activated charcoal. Clin Toxicol. 2005;43(2):61-87.',
    ],
}

if __name__ == '__main__':
    build_deck(spec, os.path.join(os.path.dirname(__file__), '21_toxicology_poisonings.pptx'))

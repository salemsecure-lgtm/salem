#!/usr/bin/env python3
"""Build Deck 18: Dehydration and Rehydration"""
import sys, os
sys.path.insert(0, os.path.dirname(__file__))
from build_deck import build_deck

spec = {
    'number': 18,
    'short_title': 'Dehydration & Rehydration',
    'title': 'Dehydration and IV/Oral Rehydration Strategies',
    'subtitle': 'Clinical Assessment, Evidence-Based ORT, and When to Escalate to IV Fluids',
    'objectives': [
        'Accurately assess dehydration severity using validated clinical scales',
        'Prioritize oral rehydration therapy (ORT) as first-line — including when it seems hard',
        'Calculate fluid deficits and maintenance requirements for IV rehydration',
        'Select appropriate IV fluids using current evidence (isotonic preferred)',
        'Identify children who need IV fluids, admission, or further workup vs. safe discharge',
    ],
    'slides': [
        {'type': 'content', 'title': 'Dehydration in PEM: The Most Common Fluid Problem',
         'bullets': [
             'Dehydration from acute gastroenteritis is one of the top 5 reasons for pediatric ED visits',
             'Most dehydration is MILD and can be managed with oral rehydration therapy (ORT)',
             'ORT is underused in ED settings — evidence strongly supports ORT over IV fluids for mild-moderate dehydration',
             'The WHO estimates ORT has saved >50 million lives worldwide since the 1970s',
             'IV fluids are reserved for severe dehydration, failed ORT, or inability to tolerate PO',
             '>The best IV is the one you never need to place. Start ORT early.',
         ],
         'notes': 'ORT is one of the most important medical interventions of the 20th century. Despite this, ED physicians consistently underuse it, defaulting to IV fluids. The evidence is clear: for mild-moderate dehydration, ORT is as effective as IV fluids, faster to initiate, less painful, and allows earlier discharge. Change the culture in your department to ORT-first.'},

        {'type': 'table', 'title': 'Clinical Dehydration Assessment',
         'headers': ['Finding', 'Mild (3-5%)', 'Moderate (6-9%)', 'Severe (≥10%)'],
         'rows': [
             ['General appearance', 'Normal, thirsty', 'Restless, irritable', 'Lethargic, limp'],
             ['Eyes', 'Normal', 'Slightly sunken', 'Deeply sunken'],
             ['Tears', 'Present', 'Decreased', 'Absent'],
             ['Mucous membranes', 'Moist', 'Sticky/dry', 'Parched'],
             ['Skin turgor', 'Normal', 'Decreased (tenting)', 'Markedly decreased'],
             ['Capillary refill', '<2 seconds', '2-3 seconds', '>3 seconds'],
             ['Heart rate', 'Normal', 'Increased', 'Increased (or bradycardic if severe)'],
             ['Urine output', 'Normal/slightly decreased', 'Decreased (oliguria)', 'Minimal/anuria'],
             ['Fontanelle (if open)', 'Normal', 'Sunken', 'Very sunken'],
         ],
         'notes': 'No single finding reliably predicts dehydration — use the combination. The most useful individual signs: prolonged capillary refill, abnormal skin turgor, and abnormal respiratory pattern. The CDS (Clinical Dehydration Scale) and WHO scale are validated tools. Weight change is the gold standard (if a recent well-weight is available), but it is rarely available in the ED.'},

        {'type': 'algorithm', 'title': 'Rehydration Strategy by Severity',
         'steps': [
             'MILD (3-5%): ORT — 50 mL/kg over 4 hours (small, frequent sips; use syringe if needed)',
             'MODERATE (6-9%): ORT first (100 mL/kg over 4h); if ORT fails → IV NS 20 mL/kg bolus then reassess',
             'SEVERE (≥10%): IV NS 20 mL/kg bolus × 3 as needed; reassess after each bolus; correct deficit over 24h',
             'SHOCK: Rapid IV/IO NS 20 mL/kg push; repeat as needed; consider blood products if hemorrhagic',
             'After initial resuscitation: calculate remaining deficit + maintenance; use isotonic fluids (NS or LR)',
             'Reassess frequently: mental status, urine output, vital signs, weight if possible',
         ],
         'notes': 'The key teaching: start ORT for mild-moderate dehydration. Even for moderate dehydration, a trial of ORT is appropriate before escalating to IV fluids. If the child vomits during ORT, wait 20 minutes and try again. If ORT truly fails (persistent vomiting, worsening clinical status), then proceed to IV fluids. Ondansetron before ORT reduces vomiting and improves ORT success rates.'},

        {'type': 'key_point', 'title': 'Evidence-Based Pearl',
         'point': 'Ondansetron + ORT is a powerful combination that reduces IV fluid use and hospitalization.',
         'sub': 'A single dose of ondansetron (0.15 mg/kg IV/PO, max 4 mg) before starting ORT significantly reduces vomiting, improves oral intake, and reduces the need for IV fluids. Freedman et al., NEJM 2006. Give ondansetron early to set ORT up for success.',
         'notes': 'This is one of the most practice-changing studies in PEM. Ondansetron is safe in children (no increase in serious adverse events), reduces vomiting within 15-30 minutes, and allows successful ORT in children who were initially unable to tolerate PO. The ODT (orally disintegrating tablet) formulation is ideal for children who are actively vomiting.'},

        {'type': 'content', 'title': 'IV Fluid Selection: Current Evidence',
         'bullets': [
             'Isotonic fluids (NS or LR) are standard for both bolus AND maintenance in most situations',
             'Hypotonic maintenance fluids (D5 0.2NS) are associated with hospital-acquired hyponatremia — AVOID',
             'AAP 2018 guideline recommends isotonic fluids (D5NS or D5LR) for maintenance in most children',
             'Exception: neonates <28 days may need D10 with lower sodium depending on clinical context',
             'Dextrose: add D5 or D10 to maintenance fluids to prevent hypoglycemia and ketosis',
             'Potassium: add 20 mEq/L KCl to maintenance fluids once urine output is confirmed',
             '>Gone are the days of D5 1/4NS as standard maintenance — isotonic fluids are the new standard',
         ],
         'notes': 'The 2018 AAP guideline was a paradigm shift. For decades, hypotonic maintenance fluids were standard based on the Holliday-Segar formula. But hospitalized children often have elevated ADH (from pain, nausea, illness), which impairs free water excretion. Hypotonic fluids in this setting cause iatrogenic hyponatremia — which can be fatal. Isotonic fluids are now the standard.'},

        {'type': 'table', 'title': 'Maintenance Fluid Calculation (Holliday-Segar)',
         'headers': ['Weight', 'Hourly Rate', '24-Hour Volume', 'Example'],
         'rows': [
             ['First 10 kg', '4 mL/kg/hr', '100 mL/kg/day', '10 kg child = 40 mL/hr = 1000 mL/day'],
             ['Next 10 kg (11-20 kg)', '2 mL/kg/hr', '50 mL/kg/day', '20 kg child = 60 mL/hr = 1500 mL/day'],
             ['Each kg above 20 kg', '1 mL/kg/hr', '20 mL/kg/day', '30 kg child = 70 mL/hr = 1700 mL/day'],
         ],
         'notes': 'The Holliday-Segar formula determines maintenance VOLUME, not type. The fluid TYPE should be isotonic (D5NS) per current guidelines. For rehydration, total fluid = maintenance + deficit replacement. Deficit volume = estimated % dehydration × weight in kg × 10. Example: 10 kg child with 7% dehydration → deficit = 0.07 × 10 × 1000 = 700 mL, given over 24 hours in addition to maintenance.'},

        {'type': 'two_column', 'title': 'ORT Success Tips vs. When to Switch to IV',
         'left_title': 'TIPS FOR ORT SUCCESS',
         'left': [
             'Give ondansetron first if vomiting',
             'Use a syringe: 5-10 mL every 2-3 minutes',
             'Use ORS (Pedialyte) — not juice, sports drinks, or water',
             'Make it a game with the child; involve parents',
             'If child vomits, wait 20 min and restart at smaller volumes',
             'Popsicles count (if made from ORS)',
             'Breastfeed ad lib in addition to ORS for breastfed infants',
         ],
         'right_title': 'SWITCH TO IV WHEN',
         'right': [
             'Persistent vomiting despite ondansetron and ORT trial',
             'Severe dehydration (≥10%)',
             'Hemodynamic instability (tachycardia, poor perfusion)',
             'Altered mental status',
             'Inability to keep up with ongoing losses',
             'Surgical abdomen suspected',
             'Electrolyte derangement requiring IV correction',
         ],
         'notes': 'The syringe technique is the single most important ORT skill. Giving 5 mL every 2-3 minutes = ~150 mL/hour — which is significant. Parents can do this at the bedside while you see other patients. Child life specialists can help make it engaging. The goal is not to stop the vomiting and diarrhea — it is to replace more fluid than is being lost.'},

        {'type': 'case', 'title': '14-Month-Old with 3 Days of Diarrhea',
         'scenario': 'A 14-month-old girl (10 kg baseline weight, 9.3 kg today) presents with 3 days of watery diarrhea (8-10 episodes/day) and vomiting (4-5 times today). She is irritable but consolable, has sunken eyes, dry mucous membranes, and decreased skin turgor. CRT 2.5 seconds. Last wet diaper was 6 hours ago. Vitals: HR 150, BP 90/55, RR 28, T 38.3°C.',
         'questions': [
             'Dehydration severity? (Moderate — 7% dehydration; weight loss 700g/10kg = 7%)',
             'Initial management? (Ondansetron 1.5 mg ODT → then ORT: 100 mL/kg = 1000 mL over 4 hours by syringe)',
             'If ORT fails after 2 hours? (IV NS 20 mL/kg bolus → then calculate deficit + maintenance)',
             'Disposition if ORT succeeds? (Home with ORS instructions, strict return precautions for poor intake/lethargy)',
         ],
         'notes': 'This case demonstrates the ideal approach: start with ORT, give ondansetron to support it, and only escalate to IV if ORT fails. Weight-based dehydration assessment is the gold standard when a recent well-weight is available. This child has a known weight loss of 7% = moderate dehydration. ORT goal: 100 mL/kg over 4 hours = ~4 mL/min by syringe.'},

        {'type': 'pitfalls', 'title': 'Dehydration Management Pitfalls',
         'items': [
             'Defaulting to IV fluids without trying ORT first — ORT is first-line for mild-moderate dehydration',
             'Using hypotonic maintenance fluids (D5 0.2NS) — use isotonic fluids per AAP guidelines',
             'Not giving ondansetron before ORT in a vomiting child — it dramatically improves ORT success',
             'Overestimating dehydration severity — studies show clinicians consistently overestimate',
             'Using inappropriate oral fluids: juice, soda, sports drinks, water — use ORS (balanced electrolytes)',
             'Not reassessing after fluid resuscitation — always reassess mental status, CRT, HR, urine output',
             'Forgetting to check glucose in lethargic, dehydrated children — hypoglycemia is common',
         ],
         'notes': 'The most common pitfall is skipping ORT. The second most common: overestimating dehydration severity. Studies show that clinicians rate dehydration as moderate when it is often mild, and severe when it is often moderate. Use objective measures (weight, clinical scales) and reassess after intervention.'},
    ],
    'takeaways': [
        'ORT is first-line for mild-moderate dehydration — give ondansetron first if the child is vomiting',
        'Syringe technique: 5-10 mL every 2-3 minutes = ~150 mL/hour — effective and painless',
        'Use isotonic maintenance fluids (D5NS) per 2018 AAP guidelines — hypotonic fluids cause iatrogenic hyponatremia',
        'Severe dehydration or shock: NS 20 mL/kg bolus, reassess, repeat as needed',
        'Always check glucose in lethargic, dehydrated children — hypoglycemia is common and treatable',
    ],
    'references': [
        'Freedman SB, et al. Oral ondansetron for gastroenteritis in a pediatric emergency department. N Engl J Med. 2006;354(16):1698-1705.',
        'Feld LG, et al. Clinical practice guideline: maintenance intravenous fluids in children. Pediatrics. 2018;142(6):e20183083.',
        'Freedman SB, et al. Gastroenteritis therapies in developed countries: systematic review and meta-analysis. PLoS One. 2015;10(6):e0128754.',
        'Hartling L, et al. Oral versus intravenous rehydration for treating dehydration due to gastroenteritis in children. Cochrane Database Syst Rev. 2006;3:CD004390.',
        'Pringle K, et al. Comparing the accuracy of the three popular clinical dehydration scales in children with diarrhea. Int J Emerg Med. 2011;4:58.',
        'WHO/UNICEF. Clinical Management of Acute Diarrhoea. WHO/UNICEF Joint Statement; 2004.',
    ],
}

if __name__ == '__main__':
    build_deck(spec, os.path.join(os.path.dirname(__file__), '18_dehydration_rehydration.pptx'))

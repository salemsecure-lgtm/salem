#!/usr/bin/env python3
"""Build Deck 27: GI Bleeding and Surgical Abdomen Red Flags"""
import sys, os
sys.path.insert(0, os.path.dirname(__file__))
from build_deck import build_deck

spec = {
    'number': 27,
    'short_title': 'GI Bleeding & Surgical Abdomen',
    'title': 'GI Bleeding and Surgical Abdomen Red Flags',
    'subtitle': 'Upper vs. Lower GI Bleeding, Emergent Causes, and Decision-Making in the PED',
    'objectives': [
        'Differentiate upper from lower GI bleeding based on presentation',
        'Build an age-based differential for GI bleeding in children',
        'Identify the child with GI bleeding who needs emergent intervention vs. observation',
        'Recognize surgical abdomen red flags that require immediate consultation',
        'Manage acute GI hemorrhage: resuscitation, stabilization, and disposition',
    ],
    'slides': [
        {'type': 'content', 'title': 'GI Bleeding in Children: The PEM Framework',
         'bullets': [
             'GI bleeding in children is common but usually NOT life-threatening',
             'Most common causes: anal fissure (infants), Meckel diverticulum, colitis, swallowed maternal blood',
             'The PEM priority: determine hemodynamic significance and identify emergent causes',
             'Upper GI bleeding (UGIB): hematemesis, coffee-ground emesis, melena',
             'Lower GI bleeding (LGIB): hematochezia (bright red blood per rectum)',
             'Key first step: Is the child hemodynamically stable? → this determines urgency',
             '>Not everything red is blood. Confirm with guaiac/occult blood test when unsure.',
         ],
         'notes': 'Most GI bleeding in children is self-limited and benign. The PEM challenge is identifying the exceptions. Red foods/drinks, beets, iron supplements, and bismuth can mimic GI bleeding. Swallowed maternal blood (from cracked nipples or delivery) is a common cause of "GI bleeding" in neonates — Apt test differentiates fetal from maternal hemoglobin.'},

        {'type': 'table', 'title': 'GI Bleeding Differential by Age',
         'headers': ['Age', 'Upper GI', 'Lower GI'],
         'rows': [
             ['Neonate', 'Swallowed maternal blood, stress gastritis, coagulopathy', 'Swallowed maternal blood, NEC, anal fissure, milk protein allergy, Hirschsprung enterocolitis'],
             ['Infant (1-12mo)', 'Esophagitis, gastritis (rare)', 'Anal fissure (#1), milk protein allergy, intussusception, Meckel diverticulum'],
             ['Toddler (1-5y)', 'Esophagitis, foreign body, caustic ingestion', 'Meckel diverticulum, infectious colitis, polyp, intussusception, HSP'],
             ['School-age', 'Esophageal varices (liver disease), PUD, Mallory-Weiss', 'IBD, infectious colitis, polyp, Meckel'],
             ['Adolescent', 'PUD, esophageal varices, Mallory-Weiss, NSAID gastropathy', 'IBD (#1 in adolescents with chronic LGIB), infectious colitis'],
         ],
         'notes': 'Age-based thinking is essential. In neonates, always consider NEC (especially premature) and swallowed maternal blood. In infants, anal fissure is by far the most common cause. In toddlers, Meckel diverticulum is the most common cause of significant painless LGIB. In adolescents, IBD becomes the most important diagnosis for chronic or recurrent bleeding.'},

        {'type': 'key_point', 'title': 'Clinical Pearl',
         'point': 'Meckel diverticulum is the most common cause of significant painless lower GI bleeding in children.',
         'sub': 'Rule of 2s: 2% of population, 2 inches long, 2 feet from ileocecal valve, presents before age 2 in most symptomatic cases, 2 types of ectopic tissue (gastric and pancreatic). Diagnosed by Meckel scan (technetium-99m pertechnetate). Treatment is surgical resection.',
         'notes': 'Meckel diverticulum is a board favorite. The ectopic gastric mucosa secretes acid, causing ulceration and painless bleeding. The bleeding can be massive. Technetium scan detects ectopic gastric mucosa with ~85% sensitivity. Key: the bleeding is PAINLESS — this differentiates it from intussusception (painful) and infectious colitis (usually with diarrhea and cramps).'},

        {'type': 'algorithm', 'title': 'Approach to Acute GI Bleeding',
         'steps': [
             'ASSESS hemodynamic stability: HR, BP, perfusion, mental status',
             'UNSTABLE: IV/IO access × 2, 20 mL/kg NS bolus, type and crossmatch, activate massive transfusion if needed',
             'STABLE: focused history (volume, frequency, pain, associated symptoms, medications)',
             'DIFFERENTIATE upper vs. lower: hematemesis/melena = upper; hematochezia = lower (usually)',
             'LABS: CBC, BMP, coagulation studies, type and screen, consider LFTs',
             'IMAGING: abdominal X-ray (obstruction?), US (intussusception?), Meckel scan if painless LGIB',
             'CONSULT: GI for endoscopy (UGIB or significant LGIB), surgery if surgical cause suspected',
         ],
         'notes': 'The algorithm starts with hemodynamic assessment. A child with significant GI bleeding and tachycardia/hypotension needs resuscitation before diagnostics. NG tube placement can help differentiate upper from lower GI bleeding — blood or coffee grounds in the aspirate confirms UGIB. But a negative NG aspirate does not rule out UGIB (especially duodenal bleeding).'},

        {'type': 'content', 'title': 'Surgical Abdomen Red Flags',
         'bullets': [
             'Bilious vomiting in neonate/infant → malrotation with volvulus until proven otherwise',
             'Rigid, distended abdomen with peritonitis → perforated viscus (free air on X-ray)',
             'GI bleeding with hemodynamic instability → massive hemorrhage requiring resuscitation ± OR',
             'Non-reducible inguinal hernia with pain, vomiting → incarcerated hernia — needs emergent reduction or OR',
             'Absent bowel sounds + distension + vomiting → bowel obstruction (adhesions, hernia, volvulus)',
             'RLQ pain with fever + peritoneal signs → appendicitis with likely perforation',
             'Testicular pain + high-riding testis + absent cremasteric reflex → testicular torsion (<6h to OR)',
             '>When in doubt, call surgery early — it is always easier to cancel a consult than to delay one',
         ],
         'notes': 'These are your red flags for the surgical abdomen. Each one should trigger immediate action. The key teaching: when the clinical picture suggests a surgical emergency, call surgery BEFORE completing the workup. Do not wait for CT results to consult for a child with bilious vomiting and a distended abdomen. Testicular torsion has a 6-hour window for viability — time is critical.'},

        {'type': 'two_column', 'title': 'Upper GI Bleeding: Variceal vs. Non-Variceal',
         'left_title': 'NON-VARICEAL UGIB',
         'left': [
             'More common in children overall',
             'Causes: esophagitis, gastritis, PUD, Mallory-Weiss tear',
             'Usually self-limited',
             'Treatment: PPI (omeprazole 1 mg/kg IV), NPO, GI consult for endoscopy',
             'Mallory-Weiss: from forceful vomiting/retching; usually stops spontaneously',
         ],
         'right_title': 'VARICEAL UGIB',
         'right': [
             'Occurs in children with portal hypertension (liver disease, portal vein thrombosis)',
             'Can be MASSIVE and life-threatening',
             'Treatment: resuscitation, octreotide 1-2 mcg/kg bolus then 1-2 mcg/kg/hr',
             'Urgent endoscopy for band ligation or sclerotherapy',
             'Consider Sengstaken-Blakemore tube if uncontrolled',
             'GI and surgery consult emergently',
         ],
         'notes': 'Variceal bleeding is the most dangerous form of UGIB in children. It occurs in the context of known liver disease (biliary atresia, cirrhosis) or portal vein thrombosis. These children can exsanguinate rapidly. Octreotide reduces splanchnic blood flow and is the medical bridge to endoscopic treatment. Endoscopic band ligation is the definitive treatment.'},

        {'type': 'case', 'title': '8-Month-Old with Painless Bloody Stools',
         'scenario': 'An 8-month-old boy is brought in for 3 episodes of large-volume, painless, dark red bloody stools over the past 4 hours. He is not in pain between episodes and has no fever or vomiting. Previously healthy. On exam: pale, HR 165, BP 75/45, CRT 3 seconds, abdomen soft and non-tender. Hemoglobin: 7.2 g/dL.',
         'questions': [
             'Is this child stable? (NO — tachycardia + hypotension + Hgb 7.2 = hemorrhagic shock)',
             'Immediate management? (2 large-bore IVs, 20 mL/kg NS bolus, type and crossmatch, prepare for transfusion)',
             'Most likely diagnosis? (Meckel diverticulum — painless, significant LGIB in an infant)',
             'Diagnostic test? (Meckel scan after stabilization; surgery consult for likely surgical resection)',
             'This child likely needs emergent transfusion: 10 mL/kg pRBCs',
         ],
         'notes': 'Classic Meckel diverticulum case. The painless nature is the key feature — this is not intussusception (which would have colicky pain). The volume of bleeding is significant and the child is in hemorrhagic shock. Resuscitate first, then diagnose. Surgery should be consulted early because this child may need OR before the Meckel scan is even done.'},

        {'type': 'pitfalls', 'title': 'GI Bleeding and Surgical Abdomen Pitfalls',
         'items': [
             'Assuming all rectal bleeding in infants is anal fissure — consider milk protein allergy, Meckel, intussusception',
             'Not checking hemoglobin in any child with visible GI bleeding — clinical estimation of blood loss is unreliable',
             'Delaying surgical consultation while completing the "full workup" for a clear surgical abdomen',
             'Missing testicular torsion — always examine the genitalia in any child with lower abdominal pain',
             'Not considering ectopic pregnancy in adolescent females with abdominal pain and vaginal bleeding',
             'Overlooking NEC in the premature infant with bloody stools and abdominal distension',
             'Discharging a child with painless significant LGIB without considering Meckel diverticulum',
         ],
         'notes': 'The anal fissure trap is common — while it is the most common cause of rectal bleeding in infants, significant or recurrent bleeding deserves further evaluation. Hemoglobin should be checked in any child with more than trivial bleeding. And the surgical consultation should happen early, not after completing every lab and imaging study.'},
    ],
    'takeaways': [
        'Assess hemodynamic stability FIRST in any child with GI bleeding — resuscitate before diagnosing',
        'Meckel diverticulum: painless, significant LGIB in a child — rule of 2s; diagnose with Meckel scan',
        'Bilious vomiting + distended abdomen = surgical emergency until proven otherwise',
        'Variceal UGIB: octreotide + emergent endoscopy; non-variceal: PPI + elective endoscopy',
        'Always examine genitalia and check pregnancy test in adolescents with abdominal pain',
    ],
    'references': [
        'Boyle JT. Gastrointestinal bleeding in infants and children. Pediatr Rev. 2008;29(2):39-52.',
        'Sahn B, et al. Evaluation and management of gastrointestinal bleeding in children. Curr Opin Pediatr. 2020;32(5):619-625.',
        'Pepper VK, et al. Meckel diverticulum in children: a 30-year review. J Pediatr Surg. 2012;47(4):680-684.',
        'Bhatia V, et al. Management of variceal bleeding in children with extrahepatic portal vein obstruction. Expert Rev Gastroenterol Hepatol. 2013;7(4):321-330.',
        'Fox VL. Gastrointestinal bleeding in infancy and childhood. Gastroenterol Clin North Am. 2000;29(1):37-66.',
    ],
}

if __name__ == '__main__':
    build_deck(spec, os.path.join(os.path.dirname(__file__), '27_gi_bleeding_surgical_abdomen.pptx'))

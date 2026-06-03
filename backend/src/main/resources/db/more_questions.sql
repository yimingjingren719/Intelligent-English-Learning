USE english_platform;

-- ============================================
-- 阅读理解题 (READING) - 一篇文章带多道小题
-- ============================================

-- 阅读理解 Passage 1: AI and Future Jobs
INSERT INTO questions (question_type, content, options, correct_answer, analysis, difficulty, tags) VALUES
('READING', '<h4>Artificial Intelligence and the Future of Work</h4><p>Artificial intelligence is transforming the global workforce at an unprecedented pace. While some fear that AI will eliminate millions of jobs, experts argue that the technology will primarily change the nature of work rather than destroy it entirely. Historical patterns suggest that technological revolutions tend to create more jobs than they displace. The Industrial Revolution, for example, eliminated many agricultural and craft positions but gave rise to entirely new categories of employment in manufacturing, transportation, and services.</p><p>However, the transition is rarely painless. Workers in routinized, predictable roles are most vulnerable to automation. A 2023 study by the World Economic Forum estimated that while 85 million jobs may be displaced by AI by 2025, approximately 97 million new roles may emerge that are better adapted to the new division of labor between humans and machines.</p><p>The key to thriving in this new landscape lies in education and adaptability. Skills such as critical thinking, creativity, emotional intelligence, and complex problem-solving remain distinctively human and are likely to grow in value. Governments and corporations alike are being urged to invest in reskilling programs to prepare the workforce for the coming changes.</p>',
 NULL, 'PASSAGE', '本文为阅读理解文章，包含3道小题', 6, '阅读,人工智能,就业,科技'),

('READING', 'According to the passage, what is the main view of experts regarding AI and jobs?',
 '[{"label":"A","text":"AI will eliminate all human jobs within a decade"},{"label":"B","text":"AI will change the nature of work rather than destroy it"},{"label":"C","text":"AI will only affect manufacturing jobs"},{"label":"D","text":"AI will have no impact on the workforce"}]',
 'B', '文章第一段明确提到"the technology will primarily change the nature of work rather than destroy it entirely"。', 5, '阅读,细节理解'),

('READING', 'How many new jobs does the World Economic Forum estimate could emerge by 2025?',
 '[{"label":"A","text":"85 million"},{"label":"B","text":"97 million"},{"label":"C","text":"100 million"},{"label":"D","text":"50 million"}]',
 'B', '文章第二段提到"approximately 97 million new roles may emerge"。注意区分85 million(可能消失的工作)和97 million(可能出现的新工作)。', 4, '阅读,数字细节'),

('READING', 'Which skill is NOT mentioned as distinctively human in the passage?',
 '[{"label":"A","text":"Critical thinking"},{"label":"B","text":"Emotional intelligence"},{"label":"C","text":"Mathematical calculation"},{"label":"D","text":"Creativity"}]',
 'C', '文章最后一段列举了critical thinking、creativity、emotional intelligence和complex problem-solving，但未提及mathematical calculation。', 5, '阅读,细节排除'),

-- 阅读理解 Passage 2: Climate Change and Consumer Behavior
('READING', '<h4>The Power of Consumer Choices in Fighting Climate Change</h4><p>Individual consumer choices, when aggregated across millions of people, can significantly influence corporate behavior and environmental outcomes. A growing body of research indicates that consumers are increasingly willing to pay premium prices for sustainable products, signaling a fundamental shift in market dynamics.</p><p>A comprehensive survey conducted across 28 countries revealed that 65% of respondents have made at least some changes to their purchasing habits to reduce their environmental impact. The most common changes include reducing plastic consumption, buying locally sourced products, and choosing brands with strong sustainability credentials.</p><p>However, experts caution that individual action alone cannot solve the climate crisis. Systemic changes in energy production, transportation infrastructure, and industrial processes are essential. The concept of the "carbon footprint" was, notably, popularized by BP in the early 2000s as a way to shift responsibility from corporations to individuals — a fact that critics argue highlights the need for collective rather than purely individual solutions.</p>',
 NULL, 'PASSAGE', '本文为阅读理解文章，探讨消费者行为与气候变化的关系', 7, '阅读,气候变化,消费行为'),

('READING', 'What percentage of respondents reported changing their purchasing habits for environmental reasons?',
 '[{"label":"A","text":"50%"},{"label":"B","text":"55%"},{"label":"C","text":"65%"},{"label":"D","text":"75%"}]',
 'C', '文章第二段明确提到"65% of respondents have made at least some changes"。', 4, '阅读,数字细节'),

('READING', 'According to the passage, who popularized the concept of "carbon footprint"?',
 '[{"label":"A","text":"Environmental activists"},{"label":"B","text":"The United Nations"},{"label":"C","text":"BP (British Petroleum)"},{"label":"D","text":"The World Economic Forum"}]',
 'C', '最后一段提到"the concept of carbon footprint was popularized by BP in the early 2000s"。', 5, '阅读,事实细节'),

('READING', 'What is the main point of the final paragraph?',
 '[{"label":"A","text":"Individual actions are the only solution to climate change"},{"label":"B","text":"Corporations should be solely responsible for environmental problems"},{"label":"C","text":"Systemic changes and collective solutions are also necessary"},{"label":"D","text":"Carbon footprints are the best way to measure environmental impact"}]',
 'C', '最后一段指出个人行动不足以解决气候危机，需要系统性的变革和集体方案。', 6, '阅读,主旨理解'),

-- ============================================
-- 填空题 (FILL_BLANK)
-- ============================================
('FILL_BLANK', 'The government has taken measures to _____ the effects of the economic crisis. (缓解)',
 NULL, 'relieve', 'relieve/mitigate/alleviate 都可以表示"缓解"。此处填relieve。', 4, '词汇,动词,同义词'),

('FILL_BLANK', 'She is very _____ about the upcoming interview; she has prepared thoroughly. (自信的)',
 NULL, 'confident', 'confident 自信的。注意与confidential(机密的)区分。', 3, '词汇,形容词,情绪'),

('FILL_BLANK', 'The company plans to _____ its business to overseas markets next year. (扩展)',
 NULL, 'expand', 'expand 扩张、扩展。注意：extend强调时间或空间的延伸，expand强调规模的扩大。', 4, '词汇,动词,商业'),

('FILL_BLANK', 'It is widely _____ that regular exercise is beneficial to both physical and mental health. (承认)',
 NULL, 'acknowledged', 'It is widely acknowledged that... 这是一个固定句型，意为"人们普遍认为..."。', 5, '语法,固定句型,被动语态'),

('FILL_BLANK', 'The scientist made a significant _____ to the field of renewable energy. (贡献)',
 NULL, 'contribution', 'make a contribution to... 对...做出贡献。注意使用名词形式。', 3, '词汇,名词,固定搭配'),

('FILL_BLANK', 'Despite the difficulties, he _____ in completing the project on time. (成功)',
 NULL, 'succeeded', 'succeed in doing sth 成功做某事。注意介词in后接动名词。', 4, '词汇,动词,固定搭配'),

('FILL_BLANK', 'The new law is designed to _____ consumers from fraudulent business practices. (保护)',
 NULL, 'protect', 'protect...from... 保护...免受...', 3, '词汇,动词,固定搭配'),

('FILL_BLANK', 'His explanation was so _____ that nobody understood what he meant. (模糊的)',
 NULL, 'vague', 'vague 模糊的、不明确的。近义词：ambiguous(歧义的)、obscure(晦涩的)。', 5, '词汇,形容词,近义词辨析'),

('FILL_BLANK', 'The two countries have reached an _____ on trade issues after months of negotiation. (协议)',
 NULL, 'agreement', 'reach an agreement 达成协议。注意冠词an的使用(agreement以元音开头)。', 3, '词汇,名词,时事'),

('FILL_BLANK', 'She could not _____ the temptation to check her phone during the meeting. (抵抗)',
 NULL, 'resist', 'resist the temptation 抵制诱惑。can not resist doing sth 忍不住做某事。', 4, '词汇,动词,固定搭配'),

-- 更多单选题
('SINGLE_CHOICE', 'The manager insisted that the report _____ before Friday.',
 '[{"label":"A","text":"be finished"},{"label":"B","text":"finished"},{"label":"C","text":"would be finished"},{"label":"D","text":"was finished"}]',
 'A', 'insist表示"坚持要求"时，从句用虚拟语气(should)+动词原形，should可省略。', 6, '语法,虚拟语气,名词性从句'),

('SINGLE_CHOICE', 'Had I known about the traffic jam, I _____ earlier.',
 '[{"label":"A","text":"would leave"},{"label":"B","text":"would have left"},{"label":"C","text":"left"},{"label":"D","text":"had left"}]',
 'B', '省略if的虚拟条件句，对过去的虚拟，主句用would have done。原句=If I had known。', 7, '语法,虚拟语气,倒装省略'),

('SINGLE_CHOICE', '_____ the weather, the outdoor concert will be postponed to next week.',
 '[{"label":"A","text":"In case of"},{"label":"B","text":"In terms of"},{"label":"C","text":"In the event of"},{"label":"D","text":"Regardless of"}]',
 'C', 'in the event of 如果...发生。in case of 以防，in terms of 就...而言，regardless of 不管。此处表示如果天气不好音乐会延期。', 5, '词汇,介词短语'),

('SINGLE_CHOICE', 'The ancient temple, _____ roof was damaged in the earthquake, is now under restoration.',
 '[{"label":"A","text":"which"},{"label":"B","text":"whose"},{"label":"C","text":"that"},{"label":"D","text":"its"}]',
 'B', '非限制性定语从句，表示所属关系用whose。whose roof = the roof of which。', 5, '语法,定语从句,关系代词');

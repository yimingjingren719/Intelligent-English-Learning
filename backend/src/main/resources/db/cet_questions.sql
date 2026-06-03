-- ============================================
-- CET-4 / CET-6 英语题库
-- 插入到 english_platform 数据库
-- ============================================
USE english_platform;

-- 单选题 (SINGLE_CHOICE)
INSERT INTO questions (question_type, content, options, correct_answer, analysis, difficulty, tags) VALUES

-- 语法 - 时态
('SINGLE_CHOICE', 'By the time he arrives, we _____ for two hours.',
 '[{"label":"A","text":"will wait"},{"label":"B","text":"will have been waiting"},{"label":"C","text":"have waited"},{"label":"D","text":"waited"}]',
 'B', 'by the time引导时间状语从句，主句用将来完成进行时，表示到将来某个时间点为止一直在进行的动作。', 6, '语法,时态,将来完成进行时'),

('SINGLE_CHOICE', 'The professor, together with his students, _____ the laboratory now.',
 '[{"label":"A","text":"are visiting"},{"label":"B","text":"is visiting"},{"label":"C","text":"visit"},{"label":"D","text":"visiting"}]',
 'B', '主语后跟有together with时，谓语动词单复数取决于主语的单复数。The professor是单数，用is visiting。', 4, '语法,主谓一致'),

('SINGLE_CHOICE', 'I wish I _____ harder when I was in college.',
 '[{"label":"A","text":"studied"},{"label":"B","text":"had studied"},{"label":"C","text":"study"},{"label":"D","text":"would study"}]',
 'B', 'wish表示对过去的虚拟，用had done结构。', 5, '语法,虚拟语气,wish'),

('SINGLE_CHOICE', 'It is high time that we _____ action to protect the environment.',
 '[{"label":"A","text":"take"},{"label":"B","text":"took"},{"label":"C","text":"taken"},{"label":"D","text":"will take"}]',
 'B', 'It is high time that...结构中，从句用一般过去时表示虚拟语气。', 6, '语法,虚拟语气,固定句型'),

('SINGLE_CHOICE', 'The number of students who _____ interested in AI _____ increasing rapidly.',
 '[{"label":"A","text":"is; is"},{"label":"B","text":"are; is"},{"label":"C","text":"are; are"},{"label":"D","text":"is; are"}]',
 'B', 'who引导的定语从句修饰students，从句谓语用复数are。The number of...作主语时谓语用单数is。', 6, '语法,主谓一致,定语从句'),

-- 词汇
('SINGLE_CHOICE', 'The new policy is expected to _____ economic growth in the coming year.',
 '[{"label":"A","text":"stimulate"},{"label":"B","text":"simulate"},{"label":"C","text":"accumulate"},{"label":"D","text":"regulate"}]',
 'A', 'stimulate 刺激，促进。simulate 模拟，accumulate 积累，regulate 调节。此处意为刺激经济增长。', 4, '词汇,近义词辨析'),

('SINGLE_CHOICE', 'He was _____ from the competition because he failed the drug test.',
 '[{"label":"A","text":"disqualified"},{"label":"B","text":"disappointed"},{"label":"C","text":"disconnected"},{"label":"D","text":"dismissed"}]',
 'A', 'disqualified 取消资格。disappointed 失望的，disconnected 断开的，dismissed 解雇的。因药检未通过被取消参赛资格。', 5, '词汇,前缀dis-'),

('SINGLE_CHOICE', 'The two companies decided to _____ in order to expand their market share.',
 '[{"label":"A","text":"merge"},{"label":"B","text":"emerge"},{"label":"C","text":"submerge"},{"label":"D","text":"urge"}]',
 'A', 'merge 合并。emerge 出现，submerge 淹没，urge 催促。两公司决定合并来扩大市场份额。', 4, '词汇,词根merge'),

('SINGLE_CHOICE', 'The medicine proved to be very _____ in treating the disease.',
 '[{"label":"A","text":"efficient"},{"label":"B","text":"sufficient"},{"label":"C","text":"effective"},{"label":"D","text":"proficient"}]',
 'C', 'effective 有效的（指效果）。efficient 高效的，sufficient 足够的，proficient 熟练的。药物对治疗这种疾病很有效。', 5, '词汇,近义词辨析,形容词'),

('SINGLE_CHOICE', 'She has a remarkable _____ for learning languages; she speaks five fluently.',
 '[{"label":"A","text":"capacity"},{"label":"B","text":"aptitude"},{"label":"C","text":"attitude"},{"label":"D","text":"altitude"}]',
 'B', 'aptitude 天赋，才能。capacity 容量/能力，attitude 态度，altitude 海拔。学习语言的天赋。', 5, '词汇,形近词辨析'),

-- 阅读理解
('SINGLE_CHOICE', 'What does the word "sustainable" most likely mean in the context of development?',
 '[{"label":"A","text":"Extremely fast"},{"label":"B","text":"Able to continue over time"},{"label":"C","text":"Based on technology alone"},{"label":"D","text":"Focused on profit only"}]',
 'B', 'sustainable 意为可持续的，在发展中指能够长期持续的。', 3, '阅读,词汇理解,上下文推断'),

('SINGLE_CHOICE', 'In academic writing, the phrase "in contrast" is used to indicate:',
 '[{"label":"A","text":"Addition"},{"label":"B","text":"Comparison showing differences"},{"label":"C","text":"Cause and effect"},{"label":"D","text":"Time sequence"}]',
 'B', 'in contrast 表示对比、对照，用于指出差异。', 3, '阅读,篇章结构,逻辑连接词'),

-- 语法 - 倒装
('SINGLE_CHOICE', 'Only after the accident _____ the importance of safety measures.',
 '[{"label":"A","text":"he realized"},{"label":"B","text":"did he realize"},{"label":"C","text":"he had realized"},{"label":"D","text":"realized he"}]',
 'B', 'Only+状语置于句首，主句需部分倒装：助动词+主语+动词原形。', 6, '语法,倒装句,only'),

('SINGLE_CHOICE', 'So difficult _____ that few students could solve it.',
 '[{"label":"A","text":"the problem was"},{"label":"B","text":"was the problem"},{"label":"C","text":"the problem is"},{"label":"D","text":"did the problem be"}]',
 'B', 'So+形容词置于句首，主句需完全倒装：系动词+主语。', 7, '语法,倒装句,so...that'),

-- 词汇 - 固定搭配
('SINGLE_CHOICE', 'The success of the project depends _____ the cooperation of all team members.',
 '[{"label":"A","text":"in"},{"label":"B","text":"on"},{"label":"C","text":"with"},{"label":"D","text":"of"}]',
 'B', 'depend on 是固定搭配，意为取决于、依赖于。', 2, '词汇,固定搭配,介词'),

('SINGLE_CHOICE', 'He was accused _____ stealing confidential information from the company.',
 '[{"label":"A","text":"for"},{"label":"B","text":"with"},{"label":"C","text":"of"},{"label":"D","text":"by"}]',
 'C', 'accuse sb of doing sth 是固定搭配，意为指控某人做某事。', 3, '词汇,固定搭配,介词'),

('SINGLE_CHOICE', 'The government is taking measures to cope _____ the aging population.',
 '[{"label":"A","text":"to"},{"label":"B","text":"with"},{"label":"C","text":"for"},{"label":"D","text":"on"}]',
 'B', 'cope with 是固定搭配，意为应对、处理。', 3, '词汇,固定搭配,介词'),

-- 语法 - 非谓语动词
('SINGLE_CHOICE', '_____ from the top of the mountain, the city looks magnificent.',
 '[{"label":"A","text":"Seeing"},{"label":"B","text":"To see"},{"label":"C","text":"Seen"},{"label":"D","text":"Having seen"}]',
 'C', '主句主语the city与see是被动关系（城市被看），用过去分词作状语表示被动。', 5, '语法,非谓语动词,分词作状语'),

('SINGLE_CHOICE', 'He regreted _____ her the truth because it hurt her feelings.',
 '[{"label":"A","text":"to tell"},{"label":"B","text":"telling"},{"label":"C","text":"tell"},{"label":"D","text":"told"}]',
 'B', 'regret doing sth 后悔做过某事。regret to do sth 遗憾要做某事。此处是后悔告诉了她真相。', 5, '语法,非谓语动词,动名词'),

('SINGLE_CHOICE', 'The meeting _____ tomorrow will focus on the budget proposal.',
 '[{"label":"A","text":"held"},{"label":"B","text":"holding"},{"label":"C","text":"to be held"},{"label":"D","text":"being held"}]',
 'C', 'tomorrow表示将来，用不定式的被动式to be held作后置定语。', 6, '语法,非谓语动词,不定式被动式'),

-- 判断题
('TRUE_FALSE', 'The word "economical" means the same as "economic".', NULL,
 'FALSE', 'economical 意为节约的、省钱的，economic 意为经济的、经济学的。两者含义不同。', 4, '词汇,近义词辨析'),

('TRUE_FALSE', 'In English, collective nouns like "team" can take either singular or plural verbs depending on whether the group is seen as a unit or as individuals.', NULL,
 'TRUE', '集合名词如team、family等，当强调整体时用单数动词，当强调个体成员时用复数动词。', 4, '语法,集合名词,主谓一致'),

('TRUE_FALSE', 'The phrase "look forward to" is followed by the base form of the verb.', NULL,
 'FALSE', 'look forward to中的to是介词，后面接名词或动名词（doing），不是动词原形。', 3, '语法,固定搭配,介词to'),

('TRUE_FALSE', 'A complex sentence contains at least one independent clause and one dependent clause.', NULL,
 'TRUE', '复合句(complex sentence)至少包含一个主句(独立分句)和一个从句(从属分句)。', 3, '语法,句型,复合句'),

('TRUE_FALSE', 'The passive voice is formed by "be + past participle".', NULL,
 'TRUE', '被动语态的基本结构是be动词+过去分词。例如：is done, was written。', 2, '语法,被动语态'),

-- 更多词汇题
('SINGLE_CHOICE', 'The scientist made a significant _____ to the field of genetics.',
 '[{"label":"A","text":"contribution"},{"label":"B","text":"distribution"},{"label":"C","text":"attribution"},{"label":"D","text":"retribution"}]',
 'A', 'contribution 贡献。distribution 分配，attribution 归因，retribution 惩罚。科学家对遗传学领域做出重大贡献。', 4, '词汇,名词,形近词'),

('SINGLE_CHOICE', 'It is essential that every student _____ the laboratory rules before starting experiments.',
 '[{"label":"A","text":"understands"},{"label":"B","text":"understand"},{"label":"C","text":"understood"},{"label":"D","text":"understanding"}]',
 'B', 'It is essential that...句型中用should+动词原形的虚拟语气，should可省略，故用understand。', 7, '语法,虚拟语气,名词性从句'),

('SINGLE_CHOICE', 'He narrowly escaped _____ in the car accident.',
 '[{"label":"A","text":"killing"},{"label":"B","text":"to be killed"},{"label":"C","text":"being killed"},{"label":"D","text":"to kill"}]',
 'C', 'escape doing sth 为固定搭配（避免某事），此处用被动being killed表示被杀死。', 6, '语法,非谓语动词,动名词被动式'),

('SINGLE_CHOICE', 'The reason _____ he was late was _____ he missed the early bus.',
 '[{"label":"A","text":"why; because"},{"label":"B","text":"that; that"},{"label":"C","text":"why; that"},{"label":"D","text":"that; because"}]',
 'C', 'The reason why...is that...是固定句型。why引导定语从句修饰reason，that引导表语从句。', 7, '语法,定语从句,表语从句,固定句型'),

-- CET-6 级别题目
('SINGLE_CHOICE', 'No sooner _____ home than it began to rain heavily.',
 '[{"label":"A","text":"had he arrived"},{"label":"B","text":"he had arrived"},{"label":"C","text":"did he arrive"},{"label":"D","text":"he arrived"}]',
 'A', 'No sooner...than...句型中，no sooner所在部分用过去完成时的部分倒装。', 7, '语法,倒装句,no sooner'),

('SINGLE_CHOICE', 'The government is committed to _____ carbon emissions by 40% by 2030.',
 '[{"label":"A","text":"reduce"},{"label":"B","text":"reducing"},{"label":"C","text":"be reduced"},{"label":"D","text":"have reduced"}]',
 'B', 'be committed to doing sth 致力于做某事，to是介词，后接动名词。', 5, '词汇,固定搭配,动名词'),

('SINGLE_CHOICE', 'His success is _____ to his perseverance rather than his intelligence.',
 '[{"label":"A","text":"contributed"},{"label":"B","text":"distributed"},{"label":"C","text":"attributed"},{"label":"D","text":"tributed"}]',
 'C', 'be attributed to 归因于。contribute to 贡献/导致，distribute 分配。他的成功归因于毅力而非才智。', 5, '词汇,动词短语,词根tribute'),

('SINGLE_CHOICE', 'Despite _____ many difficulties, she never gave up her dream.',
 '[{"label":"A","text":"she encountered"},{"label":"B","text":"encountered"},{"label":"C","text":"encountering"},{"label":"D","text":"encounter"}]',
 'C', 'despite是介词，后接名词或动名词。she encounter是正确结构但despite后不直接接从句。', 4, '语法,介词,动名词'),

('TRUE_FALSE', 'The word "compliment" is a synonym for "complement".', NULL,
 'FALSE', 'compliment 意为称赞、恭维，complement 意为补充、补足。两者发音相同但含义完全不同。', 4, '词汇,同音异义词'),

('TRUE_FALSE', 'In formal writing, contractions such as "can''t" and "won''t" should be avoided.', NULL,
 'TRUE', '在正式写作中应避免使用缩写形式，应使用完整形式cannot、will not等。', 3, '写作,正式文体'),

('TRUE_FALSE', 'The semicolon (;) can be used to join two closely related independent clauses without a conjunction.', NULL,
 'TRUE', '分号可以连接两个紧密相关的独立分句，不需要连词。例如：The sun was setting; the sky was orange.', 5, '写作,标点符号,分号');

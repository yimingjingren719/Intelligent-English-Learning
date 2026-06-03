# 智能评测与个性化英语学习平台

基于 **Vue.js + Spring Boot** 前后端分离架构，AI 驱动的英语学习系统。

## 系统架构

```
┌─────────────────────────────────────────────────┐
│                   Browser (浏览器)                │
├─────────────────────────────────────────────────┤
│  View 层: Vue.js 3 + Element Plus + ECharts     │
│  - 登录/注册  - 学习仪表盘  - 模拟测试            │
│  - 错题练习  - 生词本    - AI 答疑               │
│  - 学习日历  - 管理员题库管理                     │
├─────────────────────────────────────────────────┤
│              HTTP / JSON (Axios)                │
├─────────────────────────────────────────────────┤
│  Controller 层: Spring Boot REST API            │
│  UserController    QuestionController           │
│  TestController    ErrorController              │
│  AIController      LearningController           │
│  VocabularyController                           │
├─────────────────────────────────────────────────┤
│  Service 层: 业务逻辑                            │
│  用户管理 · 在线评测 · 自动评分 · 错题分析        │
│  学习统计 · AI 能力画像 · AI 智能答疑 · 生词管理  │
├─────────────────────────────────────────────────┤
│  DAO 层: MyBatis Plus                           │
│  UserMapper  QuestionMapper  AnswerRecordMapper │
│  ErrorRecordMapper  LearningRecordMapper        │
│  VocabularyMapper                               │
├─────────────────────────────────────────────────┤
│  Database: MySQL 8.0                            │
│  users  questions  answer_records               │
│  error_records  learning_records  vocabulary    │
└─────────────────────────────────────────────────┘
```

## 技术栈

### 前端
| 技术 | 说明 |
|------|------|
| Vue.js 3.4+ | 前端框架 |
| Vite 5.2+ | 构建工具 |
| Vue Router 4.3+ | 路由管理 |
| Pinia 2.1+ | 状态管理 |
| Element Plus 2.6+ | UI 组件库 |
| ECharts 5.5+ | 数据可视化（雷达图、柱状图、折线图） |
| Axios 1.6+ | HTTP 请求 |
| Marked 12.0+ | Markdown 渲染 |

### 后端
| 技术 | 说明 |
|------|------|
| Spring Boot 3.2.5 | 后端框架 |
| MyBatis Plus 3.5.6 | ORM 框架 |
| MySQL 8.0+ | 关系型数据库 |
| JWT 0.12.5 | 身份认证 |
| Knife4j 4.4.0 | API 文档 |
| Java HttpClient | AI API 调用（DeepSeek） |

## 功能模块

### 学生端
| 功能 | 说明 |
|------|------|
| 学习仪表盘 | 六维能力雷达图、题型正确率柱状图、模拟分数折线图、薄弱点分析、提升建议 |
| 模拟测试 | 专项练习 + 模拟试卷（选词填空/长篇匹配/仔细阅读），1小时自动交卷 |
| 错题练习 | 错题列表、错题重做、查看原题全文、标记已掌握 |
| 生词本 | 个人生词管理、词库浏览（约800个六级词汇）、单词测验（优先个人生词） |
| AI 答疑 | 基于 DeepSeek 的英语学习问答，支持语法/词汇/阅读/写作上下文 |
| 学习日历 | 月度日历视图，每日学习活动可视化 |
| 个人信息 | 昵称、性别、年龄、邮箱、个性签名 |

### 管理员端
| 功能 | 说明 |
|------|------|
| 题库管理 | 选词填空/长篇匹配/仔细阅读题目的增删改查 |
| 用户管理 | 查看注册用户列表 |

## 测试题型与分值

| 题型 | 小题数 | 每题分值 | 满分 |
|------|--------|----------|------|
| 选词填空 | 10空/篇 | 3分 | 30分/篇 |
| 长篇匹配 | 10题/篇 | 7分 | 70分/篇 |
| 仔细阅读 | 5题/篇 | 14分 | 70分/篇 |

### 模拟试卷结构
选词填空 ×1 + 长篇匹配 ×1 + 仔细阅读 ×2 = **满分 240 分**

## 能力六维面板等级标准

| 维度 | S | A | B | C | D |
|------|---|---|---|---|---|
| 选词填空正确率 | ≥80% | ≥60% | ≥40% | ≥20% | <20% |
| 长篇匹配正确率 | ≥80% | ≥60% | ≥40% | ≥20% | <20% |
| 仔细阅读正确率 | ≥80% | ≥60% | ≥40% | ≥20% | <20% |
| 模拟试卷分数 | ≥80% | ≥60% | ≥40% | ≥20% | <20% |
| 学习频率(日均大题) | ≥10 | ≥7 | ≥5 | ≥3 | <3 |
| 错题掌握率 | ≥80% | ≥60% | ≥40% | ≥20% | <20% |

## 快速开始

### 1. 环境要求
- JDK 17+
- Node.js 18+
- MySQL 8.0+
- Maven 3.8+

### 2. 数据库初始化
```bash
# 建表 + 创建管理员账号
cmd /c "mysql -u root --default-character-set=utf8mb4 < backend\src\main\resources\db\schema.sql"

# 导入六级核心词汇（约800个）
cmd /c "mysql -u root --default-character-set=utf8mb4 < backend\src\main\resources\db\cet6_words.sql"

# 导入生词本表
cmd /c "mysql -u root --default-character-set=utf8mb4 < backend\src\main\resources\db\vocabulary_schema.sql"

# 导入六级题库（选词填空+长篇匹配+仔细阅读）
cmd /c "mysql -u root --default-character-set=utf8mb4 < backend\src\main\resources\db\cet6_questions_new.sql"
```

### 3. 后端启动
```bash
cd backend
# 修改 application.yml 中的数据库密码
mvn spring-boot:run
# 访问: http://localhost:8080
# API 文档: http://localhost:8080/doc.html
```

### 4. 前端启动
```bash
cd frontend
npm install
npm run dev
# 访问: http://localhost:3000
```

### 5. 默认账号
| 角色 | 用户名 | 密码 |
|------|--------|------|
| 管理员 | admin | admin123 |
| 学生 | 自行注册 | - |

## API 接口

### 用户模块 `/api/user`
| 方法 | 路径 | 说明 |
|------|------|------|
| POST | /register | 注册 |
| POST | /login | 登录 |
| GET | /info | 获取用户信息 |
| PUT | /info | 更新用户信息 |
| GET | /list | 用户列表(管理员) |

### 题库模块 `/api/question`
| 方法 | 路径 | 说明 |
|------|------|------|
| GET | /page | 分页查询 |
| GET | /{id} | 获取详情 |
| POST | / | 新增(管理员) |
| PUT | /{id} | 更新(管理员) |
| DELETE | /{id} | 删除(管理员) |
| GET | /random | 随机获取 |

### 测试模块 `/api/test`
| 方法 | 路径 | 说明 |
|------|------|------|
| GET | /generate | 生成测试题目 |
| POST | /submit | 提交答案并评分 |
| GET | /detail/{id} | 测试详情 |

### 错题模块 `/api/error`
| 方法 | 路径 | 说明 |
|------|------|------|
| GET | /page | 分页查询 |
| GET | /{id} | 错题详情（含原题全文） |
| PUT | /{id}/master | 标记已掌握 |
| GET | /practice | 获取错题练习题目 |

### AI 模块 `/api/ai`
| 方法 | 路径 | 说明 |
|------|------|------|
| POST | /chat | 智能答疑 |
| GET | /profile | 能力画像 |
| GET | /weak-points | 薄弱点分析 |
| GET | /study-plan | 学习建议 |

### 学习记录模块 `/api/learning`
| 方法 | 路径 | 说明 |
|------|------|------|
| GET | /page | 分页查询 |
| GET | /statistics | 学习统计（支持 recentCount 参数） |
| GET | /statistics/detail | 详细统计（题型分析） |
| GET | /calendar | 日历视图数据 |
| POST | /heartbeat | 在线时长上报 |

### 生词模块 `/api/vocabulary`
| 方法 | 路径 | 说明 |
|------|------|------|
| GET | /page | 个人生词本 |
| POST | / | 添加生词 |
| DELETE | /{id} | 删除生词 |
| PUT | /{id}/master | 标记已掌握 |
| GET | /review | 待复习单词 |
| GET | /system | 系统词库 |
| POST | /learn/{id} | 从词库添加到生词本 |

## AI 配置

本系统使用 **DeepSeek** API（兼容 OpenAI 格式）。

```yaml
# application.yml
ai:
  provider: openai          # DeepSeek 兼容 OpenAI 格式
  openai:
    api-key: sk-your-key
    model: deepseek-chat
    base-url: https://api.deepseek.com
```

同样支持切换 OpenAI GPT-4 或 Google Gemini，修改 `provider` 和对应配置即可。

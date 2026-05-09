package com.xin.ai.review.constant;

/**
 * @author xyf527
 * @version 1.0
 * @description
 * @date 2026-04-22 20:58
 * @github https://github.com/xyf527
 * @copyright
 */

public class AiReviewConstants {

    private AiReviewConstants() {
    }

    // ============================
    // HTTP 请求头名称
    // ============================

    /**
     * Gitea Webhook 事件头
     */
    public static final String HEADER_GITEA_EVENT = "X-Gitea-Event";
    /**
     * GitHub Webhook 事件头
     */
    public static final String HEADER_GITHUB_EVENT = "X-GitHub-Event";
    /**
     * Gitea Token 请求头
     */
    public static final String HEADER_GITEA_TOKEN = "X-Gitea-Token";
    /**
     * GitHub Token 请求头
     */
    public static final String HEADER_GITHUB_TOKEN = "X-GitHub-Token";
    /**
     * GitLab Token 请求头
     */
    public static final String HEADER_GITLAB_TOKEN = "X-Gitlab-Token";
    /**
     * GitLab 实例地址请求头
     */
    public static final String HEADER_GITLAB_INSTANCE = "X-Gitlab-Instance";
    /**
     * Gitea 实例地址请求头
     */
    public static final String HEADER_GITEA_INSTANCE = "X-Gitea-Instance";
    /**
     * GitLab API 私有 Token 请求头
     */
    public static final String HEADER_GITLAB_PRIVATE_TOKEN = "PRIVATE-TOKEN";
    /**
     * HTTP Authorization 请求头
     */
    public static final String HEADER_AUTHORIZATION = "Authorization";
    /**
     * HTTP Accept 请求头
     */
    public static final String HEADER_ACCEPT = "Accept";
    /**
     * HTTP Content-Type 请求头
     */
    public static final String HEADER_CONTENT_TYPE = "Content-Type";
    /**
     * HTTP Charset 请求头
     */
    public static final String HEADER_CHARSET = "Charset";

    // ============================
    // HTTP 认证前缀
    // ============================

    /**
     * GitHub/Gitea 认证前缀（token 认证）
     */
    public static final String AUTH_TOKEN_PREFIX = "token ";
    /**
     * OpenAI 兼容 API 认证前缀（Bearer 认证）
     */
    public static final String AUTH_BEARER_PREFIX = "Bearer ";

    // ============================
    // HTTP Accept / Media Type 值
    // ============================

    /**
     * GitHub API Accept 头
     */
    public static final String GITHUB_ACCEPT_HEADER = "application/vnd.github.v3+json";
    /**
     * JSON 媒体类型（含编码）
     */
    public static final String MEDIA_TYPE_JSON = "application/json; charset=utf-8";
    /**
     * UTF-8 编码值
     */
    public static final String CHARSET_UTF8 = "UTF-8";

    // ============================
    // Webhook 事件类型
    // ============================

    /**
     * Push 事件
     */
    public static final String EVENT_PUSH = "push";
    /**
     * Pull Request 事件（GitHub/Gitea）
     */
    public static final String EVENT_PULL_REQUEST = "pull_request";
    /**
     * Issue Comment 事件（Gitea）
     */
    public static final String EVENT_ISSUE_COMMENT = "issue_comment";
    /**
     * Merge Request 事件（GitLab）
     */
    public static final String EVENT_MERGE_REQUEST = "merge_request";

    // ============================
    // PR / MR Action 类型
    // ============================

    /**
     * PR/MR 操作：opened（GitHub/Gitea）
     */
    public static final String ACTION_OPENED = "opened";
    /**
     * PR/MR 操作：synchronize（GitHub/Gitea）
     */
    public static final String ACTION_SYNCHRONIZE = "synchronize";
    /**
     * PR/MR 操作：edited（GitHub）
     */
    public static final String ACTION_EDITED = "edited";
    /**
     * MR 操作：open（GitLab）
     */
    public static final String ACTION_OPEN = "open";
    /**
     * MR 操作：update（GitLab）
     */
    public static final String ACTION_UPDATE = "update";
    /**
     * MR 操作：reopen（GitLab）
     */
    public static final String ACTION_REOPEN = "reopen";
    /**
     * MR 操作：merge（GitLab）
     */
    public static final String ACTION_MERGE = "merge";

    // ============================
    // 审查类型标签
    // ============================

    /**
     * 审查类型：Merge Request（GitLab）
     */
    public static final String REVIEW_TYPE_MERGE_REQUEST = "Merge Request";
    /**
     * 审查类型：Pull Request（GitHub/Gitea）
     */
    public static final String REVIEW_TYPE_PULL_REQUEST = "Pull Request";
    /**
     * 审查类型：Push
     */
    public static final String REVIEW_TYPE_PUSH = "Push";

    // ============================
    // Git 分支引用前缀
    // ============================

    /**
     * Git 分支引用前缀
     */
    public static final String GIT_REFS_HEADS_PREFIX = "refs/heads/";

    // ============================
    // API URL 路径
    // ============================

    /**
     * GitLab API v4 projects 路径前缀
     */
    public static final String GITLAB_API_PROJECTS_PATH = "/api/v4/projects/";
    /**
     * Gitea API v1 repos 路径前缀
     */
    public static final String GITEA_API_REPOS_PATH = "/api/v1/repos/";
    /**
     * GitHub 默认 API 基础地址
     */
    public static final String GITHUB_DEFAULT_API_URL = "https://api.github.com";
    /**
     * GitHub API 主机名（用于判断）
     */
    public static final String GITHUB_API_HOSTNAME = "api.github.com";
    /**
     * OpenAI 兼容 chat completions 路径
     */
    public static final String LLM_CHAT_COMPLETIONS_PATH = "/chat/completions";
    public static final String GLM_CHAT_COMPLETIONS_PATH = "/api/anthropic";

    public static final String LLM_PROVIDER_DEEPSEEK = "deepseek";
    public static final String LLM_PROVIDER_QWEN = "qwen";
    public static final String LLM_PROVIDER_GLM = "glm";
    public static final String LLM_MODEL_GLM_47_FLASH = "glm-4.7-flash";

    // ============================
    // 环境变量 Key 前缀
    // ============================

    /**
     * 钉钉 Webhook URL 环境变量前缀
     */
    public static final String ENV_DINGTALK_WEBHOOK_URL_PREFIX = "DINGTALK_WEBHOOK_URL_";
    /**
     * 企业微信 Webhook URL 环境变量前缀
     */
    public static final String ENV_WECOM_WEBHOOK_URL_PREFIX = "WECOM_WEBHOOK_URL_";
    /**
     * 飞书 Webhook URL 环境变量前缀
     */
    public static final String ENV_FEISHU_WEBHOOK_URL_PREFIX = "FEISHU_WEBHOOK_URL_";

    // ============================
    // 业务消息字符串
    // ============================

    /**
     * 服务异常通知前缀
     */
    public static final String MSG_ERROR_SERVICE_PREFIX = "AI Code Review 服务出现未知错误: ";
    /**
     * Push/PR 中无关注文件变更时的提示
     */
    public static final String MSG_NO_WATCHED_FILES_CHANGED = "关注的文件没有修改";
    /**
     * 今日无代码审查记录提示
     */
    public static final String MSG_NO_DAILY_RECORDS = "今日暂无代码审查记录";
    /**
     * 日报生成失败前缀（ReportService）
     */
    public static final String MSG_DAILY_REPORT_FAILED_PREFIX = "日报生成失败: ";
    /**
     * 代码为空提示
     */
    public static final String MSG_CODE_EMPTY = "代码为空";
    /**
     * 代码审查通知标题格式（含占位符 %s）
     */
    public static final String MSG_REVIEW_NOTIFICATION_TITLE_FORMAT = "代码审查通知 - %s";
    /**
     * 日报通知标题
     */
    public static final String MSG_DAILY_REPORT_TITLE = "代码提交日报";
    /**
     * 审查内容截断提示
     */
    public static final String MSG_CONTENT_TRUNCATED = "...(内容过长已截断)";
    /**
     * 日报兜底生成时的报告头部
     */
    public static final String MSG_DAILY_REPORT_FALLBACK_HEADER = "## 今日代码提交日报\n\n";
    /**
     * 生成日报失败前缀（ReportController）
     */
    public static final String MSG_GENERATE_DAILY_REPORT_FAILED_PREFIX = "生成日报失败: ";

    // ============================
    // Gitea Issue 相关
    // ============================

    /**
     * Gitea Review Issue PR 标题前缀
     */
    public static final String GITEA_REVIEW_ISSUE_PR_TITLE_PREFIX = "[AI Review] PR #";
    /**
     * Gitea Review Issue Push 标题前缀
     */
    public static final String GITEA_REVIEW_ISSUE_TITLE_PREFIX = "[AI Review] ";
    /**
     * Gitea Review Issue 默认 body
     */
    public static final String GITEA_REVIEW_ISSUE_DEFAULT_BODY = "AI Code Review Issue";

    // ============================
    // LLM / 提示词相关
    // ============================

    /**
     * 默认提示词 Key（无法检测到语言时使用）
     */
    public static final String PROMPT_KEY_DEFAULT = "vue3_review_prompt";
    /**
     * 兜底提示词 Key（语言特定提示词加载失败时使用）
     */
    public static final String PROMPT_KEY_FALLBACK = "code_review_prompt";
    /**
     * 提示词模板资源文件名
     */
    public static final String PROMPT_TEMPLATES_FILE = "prompt_templates.yml";
    /**
     * 语言检测默认值（未检测到语言时返回）
     */
    public static final String LANG_DEFAULT = "default";

    public static final String MODEL = "model";
    public static final String ROLE = "role";
    public static final String CONTENT = "content";
    public static final String CHOICES = "choices";
    public static final String MESSAGES = "messages";
    public static final String MESSAGE = "message";
    public static final String TEMPERATURE = "temperature";
    public static final String APPLICATION_JSON = "application/json";


    // ============================
    // 数字常量
    // ============================

    /**
     * 通知中审查结果最大展示字符数
     */
    public static final int MAX_REVIEW_RESULT_LENGTH = 3000;

}

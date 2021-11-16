package ir.fanap.podstream.util;

public class Constants {
    public static int DefaultLengthValue = 250000;
    public static String CERT_FILE = "-----BEGIN CERTIFICATE-----\n" +
            "MIIECzCCAvOgAwIBAgIUFxZBfkldSmEc6BVxINLcFpAOzlcwDQYJKoZIhvcNAQEL\n" +
            "BQAwgZQxCzAJBgNVBAYTAklSMQ8wDQYDVQQIDAZQYXJkaXMxDzANBgNVBAcMBlRl\n" +
            "aHJhbjESMBAGA1UECgwJRmFuYXBTb2Z0MRAwDgYDVQQLDAdQb2RDb3JlMRQwEgYD\n" +
            "VQQDDAtNYXNvdWRBbGF2aTEnMCUGCSqGSIb3DQEJARYYYWxhdmkubWFzb3VkODlA\n" +
            "Z21haWwuY29tMB4XDTIxMDEyNTA4MjQ1M1oXDTI0MDEyNTA4MjQ1M1owgZQxCzAJ\n" +
            "BgNVBAYTAklSMQ8wDQYDVQQIDAZQYXJkaXMxDzANBgNVBAcMBlRlaHJhbjESMBAG\n" +
            "A1UECgwJRmFuYXBTb2Z0MRAwDgYDVQQLDAdQb2RDb3JlMRQwEgYDVQQDDAtNYXNv\n" +
            "dWRBbGF2aTEnMCUGCSqGSIb3DQEJARYYYWxhdmkubWFzb3VkODlAZ21haWwuY29t\n" +
            "MIIBIjANBgkqhkiG9w0BAQEFAAOCAQ8AMIIBCgKCAQEAyJZzBdqoJbsu4rnJhvUb\n" +
            "U9v9VWPNZds/gssTBsLYFdqiRj8kGz/Ti4mj6C0SLjA506sri8+fzECvQKN7fCIE\n" +
            "NdXPDQ6zSNucXTyE850l8gncAvAAglGAJJW6co4gG+X9Yk2k8yx+3s+2U3BhrSP3\n" +
            "IULZEaQ/LxllBklLkF6eWftaQDgam0/hYXzNlBHRX89toUZdEdcoOJYFjMZbWxMP\n" +
            "SeP6p9MS3hQgtAltKsF9O/QHPmjFgPdiXgb4rjtpjM47a2uQSR7ff91luWRWNwp9\n" +
            "kGQIMd1IPArdRb9ZO7zbJtv9PQNEpd2BZNfKKcUlVzBsYAsWSAzx2nlU2ZzAOOP7\n" +
            "zwIDAQABo1MwUTAdBgNVHQ4EFgQUf5dRjuS1VKgBxOaSHqrjm1FpyxQwHwYDVR0j\n" +
            "BBgwFoAUf5dRjuS1VKgBxOaSHqrjm1FpyxQwDwYDVR0TAQH/BAUwAwEB/zANBgkq\n" +
            "hkiG9w0BAQsFAAOCAQEAiDkrV3GgRMHazEpcAWe41vcxFosncFHPN1YESbzzbHm6\n" +
            "ogSw5KfkUT6NHqioQls5FcdoRwF44LnxsqBaiWUZwvhlaTQ2bfvLmDBPNdKEaoTz\n" +
            "MpGKFuNhyYl1NuHVF7+7q/J5pr33CVb1h1zZ+L2T7u8LEo8ArkjlcXXuIq5H4KFD\n" +
            "XYRHXBP/3JAi29ZyfgFAf4OTMSUTjRSJSPsZ4L2bHNiNflEuasYTzh5yM2otvlXq\n" +
            "2hyaUpPjBMH4jJofhmQH0ZiDLbvFUxjH8Xq5OI8NY0PjposRrBygFOGkyO2zRC8c\n" +
            "XyulS5Bdk+9mMzN+F3qh/oxhuoBHqZcLzFMZg1T/xQ==\n" +
            "-----END CERTIFICATE-----";


    public final static int TopicResponseErrorCode = 15;
    public final static int StreamerResponseErrorCode = 16;
    public final static int TimeOutStreamer = 17;
    public final static int StreamerError = 18;

    //streamer time out
    public final static int DefaultTimeOut = 60000;
    public final static int StartStreamTimeOut = 60000;
    public final static int PrepareFileTimeOut = 60000;
    public final static int MaxStremerTimeOut = 60000;
}

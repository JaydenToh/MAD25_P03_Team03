const functions = require('firebase-functions');
const admin = require('firebase-admin');
const nodemailer = require('nodemailer');

admin.initializeApp();

// Brevo SMTP 配置
const transporter = nodemailer.createTransporter({
  host: "smtp-relay.brevo.com", // Brevo SMTP 服务器
  port: 587,                    // TLS 端口
  secure: false,               // true for 465, false for other ports
  auth: {
    user: functions.config().smtp.user,    // 你的邮箱地址
    pass: functions.config().smtp.pass     // Brevo SMTP 密钥
  }
});

// 发送 OTP 邮件的函数
exports.sendOTPEmail = functions.https.onCall(async (data, context) => {
  const { email, otp } = data;

  if (!email || !otp) {
    throw new functions.https.HttpsError(
      'invalid-argument',
      'Email and OTP are required.'
    );
  }

  try {
    // 发送邮件
    const mailOptions = {
      from: functions.config().smtp.user, // 发件人邮箱
      to: email,
      subject: 'Your Music App Verification Code',
      html: `
        <div style="font-family: Arial, sans-serif; max-width: 600px; margin: 0 auto;">
          <h2 style="color: #59168B;">Music App Verification</h2>
          <p>Your one-time verification code is:</p>
          <div style="background: linear-gradient(45deg, #59168B, #312C85);
                      padding: 20px;
                      border-radius: 8px;
                      text-align: center;
                      font-size: 24px;
                      color: white;
                      letter-spacing: 4px;">
            ${otp}
          </div>
          <p style="margin-top: 20px;">This code will expire in 5 minutes.</p>
          <hr style="margin: 30px 0;"/>
          <small>This is an automated message, please do not reply.</small>
        </div>
      `
    };

    await transporter.sendMail(mailOptions);

    // 将 OTP 存入 Firestore（带过期时间）
    await admin.firestore().collection('otps').doc(email).set({
      code: otp,
      expiresAt: admin.firestore.Timestamp.fromDate(
        new Date(Date.now() + 5 * 60 * 1000) // 5分钟后过期
      ),
      createdAt: admin.firestore.FieldValue.serverTimestamp()
    });

    return { success: true, message: 'OTP sent successfully' };
  } catch (error) {
    console.error('Error sending OTP:', error);
    throw new functions.https.HttpsError(
      'internal',
      'Failed to send OTP email.'
    );
  }
});

// 验证 OTP 的函数
exports.verifyOTPEmail = functions.https.onCall(async (data, context) => {
  const { email, otp } = data;

  if (!email || !otp) {
    throw new functions.https.HttpsError(
      'invalid-argument',
      'Email and OTP are required.'
    );
  }

  try {
    const otpDoc = await admin.firestore().collection('otps').doc(email).get();
    
    if (!otpDoc.exists) {
      return { valid: false, reason: 'No OTP found for this email' };
    }

    const otpData = otpDoc.data();
    const now = admin.firestore.Timestamp.now().toDate();

    // 检查是否过期
    if (otpData.expiresAt.toDate() < now) {
      await admin.firestore().collection('otps').doc(email).delete();
      return { valid: false, reason: 'OTP has expired' };
    }

    // 验证码匹配
    if (otpData.code === otp) {
      await admin.firestore().collection('otps').doc(email).delete(); // 防重用
      return { valid: true, reason: 'OTP verified successfully' };
    } else {
      return { valid: false, reason: 'Invalid OTP code' };
    }
  } catch (error) {
    console.error('Error verifying OTP:', error);
    throw new functions.https.HttpsError(
      'internal',
      'Failed to verify OTP.'
    );
  }
});
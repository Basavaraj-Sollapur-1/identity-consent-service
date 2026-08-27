package com.echolife.identity.service;

import org.springframework.stereotype.Service;
import javax.crypto.Mac;
import javax.crypto.spec.SecretKeySpec;
import java.nio.ByteBuffer;
import java.time.Instant;

@Service
public class TotpService {
  private static final String ALPHABET = "ABCDEFGHIJKLMNOPQRSTUVWXYZ234567";

  public boolean verify(String base32Secret, String code) {
    try {
      long counter = Instant.now().getEpochSecond() / 30;
      byte[] key = base32Decode(base32Secret);
      byte[] msg = ByteBuffer.allocate(8).putLong(counter).array();
      Mac mac = Mac.getInstance("HmacSHA1");
      mac.init(new SecretKeySpec(key, "HmacSHA1"));
      byte[] hash = mac.doFinal(msg);
      int offset = hash[hash.length - 1] & 0x0f;
      int binary = ((hash[offset] & 0x7f) << 24)
          | ((hash[offset + 1] & 0xff) << 16)
          | ((hash[offset + 2] & 0xff) << 8)
          | (hash[offset + 3] & 0xff);
      return String.format("%06d", binary % 1_000_000).equals(code);
    } catch (Exception e) {
      return false;
    }
  }

  public String generateSecret() {
    byte[] raw = new byte[20];
    new java.security.SecureRandom().nextBytes(raw);
    return base32Encode(raw);
  }

  private static String base32Encode(byte[] data) {
    StringBuilder out = new StringBuilder((data.length + 4) / 5 * 8);
    int buffer = 0, bits = 0;
    for (byte b : data) {
      buffer = (buffer << 8) | (b & 0xff);
      bits += 8;
      while (bits >= 5) { out.append(ALPHABET.charAt((buffer >> (bits -= 5)) & 31)); }
    }
    if (bits > 0) out.append(ALPHABET.charAt((buffer << (5 - bits)) & 31));
    return out.toString();
  }

  private static byte[] base32Decode(String s) {
    int buffer = 0, bits = 0, count = 0;
    byte[] out = new byte[s.length() * 5 / 8];
    for (char ch : s.toUpperCase().replace("=", "").toCharArray()) {
      int val = ALPHABET.indexOf(ch); if (val < 0) throw new IllegalArgumentException("Invalid base32");
      buffer = (buffer << 5) | val; bits += 5;
      if (bits >= 8) { bits -= 8; out[count++] = (byte) ((buffer >> bits) & 0xff); }
    }
    return out;
  }
}

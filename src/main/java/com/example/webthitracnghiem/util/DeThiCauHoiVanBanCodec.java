package com.example.webthitracnghiem.util;

import com.example.webthitracnghiem.dto.DeThiCauHoiDTO;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Serialize / parse nội dung câu hỏi trắc nghiệm trong đề thi dạng văn bản thô.
 * Định dạng:
 * <pre>
 * 1. Nội dung câu hỏi
 * A. Lựa chọn A
 * *B. Đáp án đúng
 * C. ...
 * D. ...
 *
 * 2. Câu tiếp theo...
 * </pre>
 */
public final class DeThiCauHoiVanBanCodec {

    private static final Pattern DONG_SO_CAU = Pattern.compile("^\\d+\\.\\s*(.*)$");
    private static final Pattern DONG_LUA_CHON = Pattern.compile("^(\\*)?([A-Da-d])\\.\\s*(.*)$");

    private DeThiCauHoiVanBanCodec() {
    }

    public static String serialize(List<DeThiCauHoiDTO> ordered) {
        if (ordered == null || ordered.isEmpty()) {
            return "";
        }
        StringBuilder sb = new StringBuilder();
        for (int i = 0; i < ordered.size(); i++) {
            DeThiCauHoiDTO c = ordered.get(i);
            sb.append(i + 1).append(". ").append(oneLine(c.getNoiDung())).append('\n');
            String d = chuanHoaDapAn(c.getDapAnDung());
            appendLuaChon(sb, "A", c.getLuaChonA(), "A".equals(d));
            appendLuaChon(sb, "B", c.getLuaChonB(), "B".equals(d));
            appendLuaChon(sb, "C", c.getLuaChonC(), "C".equals(d));
            appendLuaChon(sb, "D", c.getLuaChonD(), "D".equals(d));
            sb.append('\n');
        }
        return sb.toString().trim();
    }

    private static void appendLuaChon(StringBuilder sb, String ch, String text, boolean dung) {
        if (dung) {
            sb.append('*');
        }
        sb.append(ch).append(". ").append(oneLine(text)).append('\n');
    }

    private static String oneLine(String s) {
        if (s == null) {
            return "";
        }
        return s.replace('\r', ' ').replace('\n', ' ').trim();
    }

    private static String chuanHoaDapAn(String d) {
        if (d == null || d.isBlank()) {
            return "";
        }
        return d.trim().substring(0, 1).toUpperCase(Locale.ROOT);
    }

    /**
     * @return lỗi tiếng Việt nếu không parse được; null nếu OK
     */
    public static String parse(String raw, List<ParsedMcq> out) {
        out.clear();
        if (raw == null || raw.isBlank()) {
            return null;
        }
        String[] lines = raw.split("\\R");
        ParsedMcq cur = null;
        StringBuilder stem = new StringBuilder();
        boolean nhieuDapAn = false;

        for (String line : lines) {
            String t = line.trim();
            if (t.isEmpty()) {
                continue;
            }

            Matcher mq = DONG_SO_CAU.matcher(t);
            if (mq.matches()) {
                if (cur != null) {
                    String err = ketThucCau(cur, stem, nhieuDapAn);
                    if (err != null) {
                        return err;
                    }
                    out.add(cur);
                }
                cur = new ParsedMcq();
                stem = new StringBuilder(mq.group(1).trim());
                nhieuDapAn = false;
                continue;
            }

            Matcher mo = DONG_LUA_CHON.matcher(t);
            if (mo.matches() && cur != null) {
                flushStem(cur, stem);
                String letter = mo.group(2).toUpperCase(Locale.ROOT);
                String noiDungOpt = mo.group(3).trim();
                boolean star = mo.group(1) != null && !mo.group(1).isEmpty();
                if (star) {
                    if (cur.dapAnDung != null) {
                        nhieuDapAn = true;
                    }
                    cur.dapAnDung = letter;
                }
                switch (letter) {
                    case "A" -> cur.luaChonA = noiDungOpt;
                    case "B" -> cur.luaChonB = noiDungOpt;
                    case "C" -> cur.luaChonC = noiDungOpt;
                    case "D" -> cur.luaChonD = noiDungOpt;
                    default -> { }
                }
                continue;
            }

            if (cur == null) {
                continue;
            }
            if (stem.length() > 0) {
                stem.append(' ');
            }
            stem.append(t);
        }

        if (cur != null) {
            String err = ketThucCau(cur, stem, nhieuDapAn);
            if (err != null) {
                return err;
            }
            out.add(cur);
        }

        if (out.isEmpty()) {
            return "Không tìm thấy câu hỏi hợp lệ. Mỗi câu bắt đầu bằng số thứ tự (vd: 1. Nội dung...).";
        }
        return null;
    }

    private static void flushStem(ParsedMcq cur, StringBuilder stem) {
        if (stem.length() == 0) {
            return;
        }
        if (cur.noiDung == null || cur.noiDung.isEmpty()) {
            cur.noiDung = stem.toString().trim();
        } else {
            cur.noiDung = (cur.noiDung + " " + stem.toString()).trim();
        }
        stem.setLength(0);
    }

    private static String ketThucCau(ParsedMcq cur, StringBuilder stem, boolean nhieuDapAn) {
        flushStem(cur, stem);
        if (nhieuDapAn) {
            return "Mỗi câu chỉ được đánh dấu một đáp án đúng (một dòng có dấu *).";
        }
        if (cur.noiDung == null || cur.noiDung.isBlank()) {
            return "Có câu hỏi thiếu nội dung.";
        }
        if (cur.luaChonA == null || cur.luaChonA.isBlank()
                || cur.luaChonB == null || cur.luaChonB.isBlank()) {
            return "Mỗi câu cần ít nhất lựa chọn A và B.";
        }
        if (cur.dapAnDung == null || cur.dapAnDung.isBlank()) {
            return "Mỗi câu cần đánh dấu đáp án đúng bằng * trước chữ cái (vd: *B.).";
        }
        return null;
    }

    public static final class ParsedMcq {
        private String noiDung;
        private String luaChonA;
        private String luaChonB;
        private String luaChonC;
        private String luaChonD;
        private String dapAnDung;

        public String getNoiDung() {
            return noiDung;
        }

        public void setNoiDung(String noiDung) {
            this.noiDung = noiDung;
        }

        public String getLuaChonA() {
            return luaChonA;
        }

        public String getLuaChonB() {
            return luaChonB;
        }

        public String getLuaChonC() {
            return luaChonC;
        }

        public String getLuaChonD() {
            return luaChonD;
        }

        public String getDapAnDung() {
            return dapAnDung;
        }
    }
}

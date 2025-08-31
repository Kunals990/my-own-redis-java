package store;

import java.util.Objects;

public class MemberScore implements Comparable<MemberScore> {
    private final String member;
    private final double score;

    public MemberScore(String member, double score) {
        this.member = member;
        this.score = score;
    }

    public String getMember() {
        return member;
    }

    public double getScore() {
        return score;
    }

    @Override
    public int compareTo(MemberScore other) {
        int scoreCompare = Double.compare(this.score, other.score);
        if (scoreCompare != 0) {
            return scoreCompare;
        }
        return this.member.compareTo(other.member);
    }
    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        MemberScore that = (MemberScore) o;
        return Double.compare(that.score, score) == 0 &&
                Objects.equals(member, that.member);
    }

    @Override
    public int hashCode() {
        return Objects.hash(member, score);
    }
}
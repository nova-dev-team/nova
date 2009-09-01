class Fixnum

public

  def to_pretty_file_size
    def pretty_val val
      if val > 100
        val.round
      else
        (val * 10.0).round / 10.0
      end
    end

    if self < 1000
      return self.to_s
    elsif self < 1000 * 1000
      val = pretty_val(self / 1024.0)
      return val.to_s + "K"
    elsif self < 1000 * 1000 * 1000
      val = pretty_val(self / 1024.0 / 1024.0)
      return val.to_s + "M"
    elsif self < 1000 * 1000 * 1000 * 1000
      val = pretty_val(self / 1024.0 / 1024.0 / 1024.0)
      return val.to_s + "G"
    end
  end

end

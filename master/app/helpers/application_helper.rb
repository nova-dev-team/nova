# Methods added to this helper will be available to all templates in the application.
module ApplicationHelper

  def ApplicationHelper::client_browser_name request
    user_agent = request.env['HTTP_USER_AGENT'].downcase
    if user_agent =~ /msie/i 
      "Internet Explorer"
    elsif user_agent =~ /konqueror/i 
      "Konqueror" 
    elsif user_agent =~ /gecko/i
      "Mozilla"
    elsif user_agent =~ /opera/i
      "Opera"
    elsif user_agent =~ /applewebkit/i
      "Safari"
    else
      "Unknown"
    end
  end

end

class ApplicationController < ActionController::Base
  def hellow
    render html: "hello, world!"
  end
end

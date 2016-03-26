require_relative 'test_helper'
class VotesAPITest < MiniTest::Unit::TestCase
  include Rack::Test::Methods
  include TestHelper

  def app
    Sinatra::Application
  end

  def test_votes

	post '/sendVoteFromGui'


  end

end

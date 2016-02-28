
class VotesAPITest < MiniTest::Unit::TestCase
  include Rack::Test::Methods
  include TestHelper

  def app
    Sinatra::Application
  end

  def test_votes
    
    post "/sendVote", ballot_box: 4, serial_number: 12345, votes: [{vote_value: "qwert"}, {vote_value: "qazwsx"}]
    assert last_response.ok?
	post "/sendVote", ballot_box: 2, serial_number: 54321, votes: [{vote_value: "jjjj"}, {vote_value: "vvvv"}]
    assert last_response.ok?

    get "/getBBVotes", race_id: -1
    assert last_response.ok?
    parsed = JSON.parse last_response.body
    assert_equal 4, parsed.count, "test_votes1"

    assert_equal ["balot_box", "serial_number", "votes"],  parsed[ 0 ].keys, "test_votes1"
    assert_equal 4,      parsed[ 0 ][ "ballot_box" ], "test_votes2"
    assert_equal 12345,            parsed[ 0 ][ "serial_number" ], "test_votes3"
	assert_equal ["qwert", "qazwsx"],     parsed[ 0 ][ "votes" ], "test_votes4"


    get "/getBBVotes", race_id: 2
    assert last_response.ok?
    parsed = JSON.parse last_response.body
    assert_equal 2, parsed.count, "test_votes5"

    assert_equal ["balot_box", "serial_number", "votes"],  parsed[ 0 ].keys, "test_votes6"
	assert_equal 4,      parsed[ 0 ][ "ballot_box" ], "test_votes7"
    assert_equal 2,      parsed[ 1 ][ "ballot_box" ], "test_votes8"
    assert_equal 12345,            parsed[ 0 ][ "serial_number" ], "test_votes9"
	assert_equal 54321,            parsed[ 1 ][ "serial_number" ], "test_votes10"
	assert_equal ["qwert"],     parsed[ 0 ][ "votes" ], "test_votes11"
	assert_equal ["vvvv"],     parsed[ 1 ][ "votes" ], "test_votes12"

  end

end

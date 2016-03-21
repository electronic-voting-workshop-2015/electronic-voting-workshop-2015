require_relative 'test_helper'
class VotesAPITest < MiniTest::Unit::TestCase
  include Rack::Test::Methods
  include TestHelper

  def app
    Sinatra::Application
  end

  def test_votes
=begin
    post "/sendVote", ballot_box: 4, serial_number: 12345, votes: [{vote_value: "qwert"}, {vote_value: "qazwsx"}]
    assert last_response.ok?
	post "/sendVote", ballot_box: 2, serial_number: 54321, votes: [{vote_value: "jjjj"}, {vote_value: "vvvv"}]
    assert last_response.ok?

    get "/getBBVotes/-1"
    assert last_response.ok?, "test_votes -1"
    parsed = JSON.parse last_response.body
    assert_equal 4, parsed.count, "test_votes0"
	#puts parsed

    assert_equal ["vote_id", "vote_value", "ballot_box", "serial_number", "race_id"],  parsed[ 0 ].keys, "test_votes1"
    assert_equal 4,      parsed[ 0 ][ "ballot_box" ], "test_votes2"
    assert_equal 12345,            parsed[ 0 ][ "serial_number" ], "test_votes3"
	assert_equal "qwert",     parsed[ 0 ][ "vote_value" ], "test_votes4"


	get "/getBBVotes/2"
    assert last_response.ok?
    parsed = JSON.parse last_response.body
    assert_equal 2, parsed.count, "test_votes5"
	#puts parsed

    assert_equal ["vote_id", "vote_value", "ballot_box", "serial_number", "race_id"],  parsed[ 0 ].keys, "test_votes6"
	assert_equal 4,      parsed[ 0 ][ "ballot_box" ], "test_votes7"
    assert_equal 2,      parsed[ 1 ][ "ballot_box" ], "test_votes8"
    assert_equal 12345,            parsed[ 0 ][ "serial_number" ], "test_votes9"
	assert_equal 54321,            parsed[ 1 ][ "serial_number" ], "test_votes10"
	assert_equal "qazwsx",     parsed[ 0 ][ "vote_value" ], "test_votes11"
	assert_equal "vvvv",     parsed[ 1 ][ "vote_value" ], "test_votes12"
=end
  end

	def test_zkp
=begin    	
		get "/getBBVotes/-1"
		allVotes = JSON.parse last_response.body

		for index in 0 ... allVotes.count
			post "/publishZKP", vote_id: allVotes[index]["vote_id"], party_id: index+1, zkp: allVotes[index]["vote_value"]+"#{index}", 
								race_id: allVotes[index]["race_id"], signature: "this is a signature"
			assert last_response.ok?
		end

		get "/getZKP"
		assert last_response.ok?
		allZKP = JSON.parse last_response.body
		assert_equal allVotes.count, allZKP.count
=end
  	end

end

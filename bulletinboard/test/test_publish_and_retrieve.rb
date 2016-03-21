require 'test/test_helper'

class PublishRetrieveAPITest < MiniTest::Unit::TestCase
  include Rack::Test::Methods
  include TestHelper

  def app
    Sinatra::Application
  end

  def test_save_and_retrieve_arbitrary_json_objects
    party_id = randomString
    signature = randomString

    arbitraryJSONs = [ Commitment, Complaint, SecretCommitment ]
    
    arbitraryJSONs.each do |model|
        post "/publish#{model}", party_id: party_id, singature: signature, content: { numbers: ["5", "6"], names: ["Yosi", "Haim"] }
        assert last_response.ok?
        post "/publish#{model}", party_id: party_id, signature: signature, content: { numbers: ["7", "8"], faces: ["nice", "angry"] }
        assert last_response.ok?

        get "/retrieve#{model}"
        assert last_response.ok?
        parsed = JSON.parse last_response.body
        assert_equal 2, parsed.count

        assert_equal ["numbers", "names"],  parsed[ 0 ].keys
        assert_equal ["Yosi", "Haim"],      parsed[ 0 ][ "names" ]
        assert_equal ["5", "6"],            parsed[ 0 ][ "numbers" ]

        assert_equal ["numbers", "faces"],  parsed[ 1 ].keys
        assert_equal ["nice", "angry"],     parsed[ 1 ][ "faces" ]
        assert_equal ["7", "8"],            parsed[ 1 ][ "numbers" ]
    end
  end

  def test_retrieve_specific_message
    create_some_messages [
        { party_id: 7, recepient_id: 8, message: 'Hi 8 from 7', signature: 'signed!' },
        { party_id: 7, recepient_id: 9, message: 'Hi 9 from 7', signature: 'signed!' },
        { party_id: 8, recepient_id: 9, message: 'Hi 9 from 8', signature: 'signed!' },
        { party_id: 8, recepient_id: 7, message: 'Hi 7 from 8', signature: 'signed!' },
    ]

    get "/retrieveMessage", recepient_id: 9
    assert last_response.ok?
    parsed = JSON.parse last_response.body
    assert_equal 2, parsed.count
    assert_equal( {"party_id"=> 7, "recepient_id"=> 9, "message"=> 'Hi 9 from 7', "signature"=> 'signed!'}, parsed[ 0 ] )
    assert_equal( {"party_id"=> 8, "recepient_id"=> 9, "message"=> 'Hi 9 from 8', "signature"=> 'signed!'}, parsed[ 1 ] )

    get "/retrieveMessage", recepient_id: 8
    assert last_response.ok?
    parsed = JSON.parse last_response.body
    assert_equal 1, parsed.count
    assert_equal( {"party_id"=> 7, "recepient_id"=> 8, "message"=> 'Hi 8 from 7', "signature"=> 'signed!'}, parsed[ 0 ] )
  end

  def test_retrieve_all_messages
    create_some_messages [
        { party_id: 7, recepient_id: 8, message: 'Hi 8 from 7', signature: 'signed!' },
        { party_id: 7, recepient_id: 9, message: 'Hi 9 from 7', signature: 'signed!' },
        { party_id: 8, recepient_id: 9, message: 'Hi 9 from 8', signature: 'signed!' },
        { party_id: 8, recepient_id: 7, message: 'Hi 7 from 8', signature: 'signed!' },
    ]
    get "/retrieveMessage"
    assert last_response.ok?
    parsed = JSON.parse last_response.body
    assert_equal 4, parsed.count
    assert_equal( {"party_id"=> 7, "recepient_id"=> 8, "message"=> 'Hi 8 from 7', "signature"=> 'signed!'}, parsed[ 0 ] )
    assert_equal( {"party_id"=> 7, "recepient_id"=> 9, "message"=> 'Hi 9 from 7', "signature"=> 'signed!'}, parsed[ 1 ] )
    assert_equal( {"party_id"=> 8, "recepient_id"=> 9, "message"=> 'Hi 9 from 8', "signature"=> 'signed!'}, parsed[ 2 ] )
    assert_equal( {"party_id"=> 8, "recepient_id"=> 7, "message"=> 'Hi 7 from 8', "signature"=> 'signed!'}, parsed[ 3 ] )
  end

  def create_some_messages( messages )
      messages.each do |message|
        Message.create!( ** message ) 
      end
  end

end

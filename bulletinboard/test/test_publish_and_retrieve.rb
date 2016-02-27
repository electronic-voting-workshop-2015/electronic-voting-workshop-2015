require 'test/test_helper'

class PublishRetrieveAPITest < MiniTest::Unit::TestCase
  include Rack::Test::Methods
  include TestHelper

  def app
    Sinatra::Application
  end

  def test_save_and_retrieve_proofs_file
    party_id = randomString
    signature = randomString

    post '/publish', party_id: party_id, singature: signature, content: { numbers: ["5", "6"], names: ["Yosi", "Haim"] }
    assert last_response.ok?
    post '/publish', party_id: party_id, signature: signature, content: { numbers: ["7", "8"], faces: ["nice", "angry"] }
    assert last_response.ok?

    get '/retrieve'
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

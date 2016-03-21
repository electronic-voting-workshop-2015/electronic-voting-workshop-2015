require 'test/test_helper'
require 'models/public_key'

class PublishRetrieveAPITest < MiniTest::Unit::TestCase
  include Rack::Test::Methods
  include TestHelper

  def app
    Sinatra::Application
  end

  def test_get_public_key
    #PublicKey.create! party_id: 10, first: 11, second: 12
    #PublicKey.create! party_id: 11, first: 13, second: 14

	post "/publishPublicKey", party_id: 10, first: "11", second: "12"
	post "/publishPublicKey", party_id: 11, first: "13", second: "14"
    
    get "/getPublicKey", party_id: 10
    assert last_response.ok?
    parsed = JSON.parse last_response.body
    assert_equal 10, parsed[ "party_id" ]
    assert_equal "11", parsed[ "first" ]
    assert_equal "12", parsed[ "second" ]

    get "/getPublicKey", party_id: 11
    assert last_response.ok?
    parsed = JSON.parse last_response.body
    assert_equal 11, parsed[ "party_id" ]
    assert_equal "13", parsed[ "first" ]
    assert_equal "14", parsed[ "second" ]
  end

end

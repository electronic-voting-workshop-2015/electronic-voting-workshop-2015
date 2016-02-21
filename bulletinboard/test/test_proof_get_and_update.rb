require 'test/test_helper'

class ProofsAPITest < MiniTest::Unit::TestCase
  include Rack::Test::Methods

  def app
    Sinatra::Application
  end

  def test_save_and_retrieve_proofs_file
    content = "#{Random.rand}"
    post '/updateProofsFile', content: content
    assert last_response.ok?

    get '/proofsFile'
    assert last_response.ok?
    parsed = JSON.parse last_response.body
    assert_equal content, parsed[ "content" ]
  end

  def test_update_existing_content
    content = "#{Random.rand}"
    post '/updateProofsFile', content: content
    assert last_response.ok?

    content = "#{Random.rand}"
    post '/updateProofsFile', content: content
    assert last_response.ok?

    get '/proofsFile'
    assert last_response.ok?
    parsed = JSON.parse last_response.body
    assert_equal content, parsed[ "content" ]
  end

end

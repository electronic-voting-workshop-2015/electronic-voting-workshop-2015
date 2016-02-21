require 'test/test_helper'

class PublishRetrieveAPITest < MiniTest::Unit::TestCase
  include Rack::Test::Methods
  include TestHelper

  def app
    Sinatra::Application
  end

  def test_save_and_retrieve_proofs_file
    content = randomString
    party_id = randomString

    post '/publish', party_id: party_id, content: content
    assert last_response.ok?
    parsed = JSON.parse last_response.body
    id = parsed[ "id" ]

    get '/retrieve', id: id
    assert last_response.ok?
    parsed = JSON.parse last_response.body
    assert_equal content, parsed[ "content" ]
  end

  def test_get_non_existing_id_results_in_error
    get '/retrieve', id: randomInteger
    assert ! last_response.ok?
  end

end

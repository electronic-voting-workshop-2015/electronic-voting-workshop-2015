require 'models/public_key'

get '/getPublicKey' do
    PublicKey.find_by( party_id: params[ "party_id" ] ).to_json
end

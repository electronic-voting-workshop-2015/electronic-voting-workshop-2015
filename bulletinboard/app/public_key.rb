require 'models/public_key'

get '/getPublicKey' do
    PublicKey.find_by( party_id: params[ "party_id" ] ).to_json
end

post '/publishPublicKey' do
	begin
		publicKet = PublicKey.create!(party_id: params[ "party_id" ], first: params[ "first" ], second: params[ "second" ])
	rescue
		raise "Error while inserting public key for party #{params[ "party_id" ]}"
	end	

end

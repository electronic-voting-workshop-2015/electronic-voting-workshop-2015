require 'models/commitment'
require 'models/message'
require 'models/complaint'
require 'models/secret_commitment'
require 'models/VotingPublicKey'
require 'models/proofs_file'
require 'models/parameters_file'

def publish_and_retrieve( model, publish_url, retrieve_url, secure: true )
    post publish_url do
		if secure 
            demand_valid_signature!( request )
        end
        if ! params.include?( "content" )
            halt 590, "no :content key!"
        end
        publication = model.create!( content: params[ "content" ].to_json )
    end

    get retrieve_url do
        all = model.all.to_a.map do |publication| 
            JSON.parse publication.content
        end
        all.to_json
    end
end

def publish_and_retrieve_without_signature( model, publish_url, retrieve_url )
    publish_and_retrieve( model, publish_url, retrieve_url, secure: false )
end

publish_and_retrieve Commitment, '/publishCommitment', '/retrieveCommitment'
publish_and_retrieve SecretCommitment, '/publishSecretCommitment', '/retrieveSecretCommitment'
publish_and_retrieve_without_signature Complaint, '/publishComplaint', '/retrieveComplaint'
publish_and_retrieve_without_signature VotingPublicKey, '/publishVotingPublicKey', '/retrieveVotingPublicKey'
publish_and_retrieve_without_signature ParametersFile, '/publishParametersFile', '/retrieveParametersFile'
publish_and_retrieve_without_signature ProofsFile, '/publishProofsFile', '/retrieveProofsFile'
